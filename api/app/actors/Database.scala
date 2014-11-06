package actors

import com.gilt.quality.models.{AdjournForm, Incident, Meeting, MeetingForm, Organization, Task}
import db.{AgendaItemsDao, IncidentsDao, FullMeetingForm, MeetingsDao, OrganizationsDao, Pager, UsersDao}
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

  private[actors] def autoAdjournMeetings() {
    val limit = 100
    val meetings = MeetingsDao.findAll(
      isUpcoming = Some(false),
      isAdjourned = Some(false),
      scheduledWithinNHours = Some(168),
      scheduledOnOrBefore = Some((new DateTime()).plusMinutes(-180)),
      limit = limit
    )
    meetings.foreach { meeting =>
      MeetingsDao.adjourn(UsersDao.Actor, meeting, AdjournForm())
    }
    if (meetings.size >= limit) {
      // We don't use Pager here as we are modifying the underlying
      // meetings directly which affects the page size
      autoAdjournMeetings()
    }
  }

  private[actors] def syncMeetingById(meetingId: Long) {
    eachMeetingIncident(meetingId, incident =>
      global.Actors.mainActor ! MeetingMessage.SyncIncident(incident.id)
    )
  }

  /**
    * Given a meeting, creates a SyncIncident message for each
    * incident in that meeting. This provides an easy way to
    * recalculate next steps for any incident in this meeting.
    */
  private[actors] def eachMeetingIncident(
    meetingId: Long,
    f: Incident => Unit
  ) {
    Pager.eachPage[Incident] { offset =>
      IncidentsDao.findAll(
        meetingId = Some(meetingId),
        limit = 100,
        offset = offset
      )
    } {
      f(_)
    }
  }

  private[actors] def nextTask(incident: Incident): Option[Task] = {
    MeetingsDao.findAll(
      incidentId = Some(incident.id),
      isAdjourned = Some(false),
      limit = 1
    ).headOption match {
      case Some(m) => {
        // Already have an active task for this incident
        None
      }
      case None => {
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
              // The result of the review plan stage is a grade for a plan
              // for the incident. If there is no grade, or the incident
              // has never had its plan reviewed, then the incident goes
              // into the next meeting.
              incident.plan.flatMap(_.grade).isEmpty || !incidentTasks.contains(t)
            }
            case Task.UNDEFINED(_) => {
              !incidentTasks.contains(t)
            }
          }
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

