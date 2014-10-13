package actors

import com.gilt.quality.models.{Incident, Meeting, MeetingForm, Organization, Task}
import db.{AgendaItemsDao, IncidentsDao, FullMeetingForm, MeetingsDao, OrganizationsDao, Pager, User}
import org.joda.time.DateTime

object Database {

  val AllTasks = Seq(Task.ReviewTeam, Task.ReviewPlan)

  private[actors] def ensureAllOrganizationHaveUpcomingMeetings() {
    Pager.eachPage[Organization] { offset =>
      OrganizationsDao.findAll(
        limit = 100,
        offset = offset
      )
    } {
      ensureOrganizationHasUpcomingMeetings(_)
    }
  }

  /**
    * Given an organization, if the organization is using the meetings
    * module, ensures that there are at least 2 meetings scheduled in
    * the future.
    */
  def ensureOrganizationHasUpcomingMeetings(org: Organization) {
    MeetingSchedule.findByOrganization(org).foreach { schedule =>
      schedule.upcomingDates.foreach { date =>
        MeetingsDao.upsert(org, date)
      }
    }
  }

  private[actors] def syncMeetings() {
    Pager.eachPage[Meeting] { offset =>
      recentlyEndedMeetings(offset)
    } { meeting =>
      syncMeeting(meeting)
    }
  }

  private[actors] def recentlyEndedMeetings(offset: Int = 0): Seq[Meeting] = {
    MeetingsDao.findAll(
      isUpcoming = Some(false),
      scheduledWithinNHours = Some(12),
      offset = offset
    )
  }

  /**
    * Given a meeting, creates a SyncIncident message for each
    * incident in that meeting. This provides an easy way to
    * recalculate next steps for any incident in this meeting.
    */
  private[actors] def syncMeeting(meeting: Meeting) {
    Pager.eachPage[Incident] { offset =>
      meetingIncidents(meeting, offset)
    } { incident =>
      global.Actors.mainActor ! actors.MeetingMessage.SyncIncident(incident.id)
    }
  }

  private[actors] def meetingIncidents(meeting: Meeting, offset: Int = 0): Seq[Incident] = {
    IncidentsDao.findAll(
      meetingId = Some(meeting.id),
      limit = 100,
      offset = offset
    )
  }

  private[actors] def nextTask(incident: Incident): Option[Task] = {
    val incidentTasks = AgendaItemsDao.findAll(
      incidentId = Some(incident.id)
    ).map(_.task).toSet

    AllTasks.find { t =>
      t match {
        case Task.ReviewTeam => {
          // If the incident does not have a team or the review
          // team task has never been a meeting for this incident,
          // then return true
          incident.team.isEmpty || !incidentTasks.contains(t)
        }
        case Task.ReviewPlan => {
          // We specifically allow empty plans - that just means a
          // team did not complete a plan. For this task, just
          // check if the incident has been in a meeting to review
          // its plan.
          !incidentTasks.contains(t)
        }
        case Task.UNDEFINED(_) => {
          !incidentTasks.contains(t)
        }
      }
    }
  }

  /**
    * Assigns this incident to an upcoming meeting if needed.
    */
  def assignIncident(incidentId: Long) {
    IncidentsDao.findById(incidentId).map { incident =>
      nextTask(incident).map { task =>
        // Only assign incidents for organizations w/ meetings
        // enabled. These orgs will always have upcoming meetings
        // (managed by the EnsureUpcoming message above)
        bestNextMeetingForOrg(incident.organization).map { meeting =>
          MeetingsDao.upsertAgendaItem(meeting, incident, task)
        }
      }
    }
  }

  private[actors] def bestNextMeetingForOrg(org: Organization): Option[Meeting] = {
    val twelveHoursFromNow = (new DateTime()).plus(3600*12*1000l)
    MeetingsDao.findAll(
      org = Some(org),
      isUpcoming = Some(true)
    ).reverse.find(_.scheduledAt.isAfter(twelveHoursFromNow))
  }

}

