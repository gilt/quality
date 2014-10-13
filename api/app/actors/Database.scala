package actors

import com.gilt.quality.models.{AgendaItemForm, Incident, Meeting, MeetingForm, Organization, Task}
import db.{FullAgendaItemForm, AgendaItemsDao, IncidentsDao, FullMeetingForm, MeetingsDao, OrganizationsDao, User}
import org.joda.time.DateTime

object Database {

  val AllTasks = Seq(Task.ReviewTeam, Task.ReviewPlan)

  private[actors] def ensureAllOrganizationHaveUpcomingMeetings() {
    OrganizationsDao.findAll().foreach { org =>
      ensureOrganizationHasUpcomingMeetings(org)
    }
  }

  private[actors] def ensureOrganizationHasUpcomingMeetings(org: Organization) {
    MeetingSchedule.findByOrganization(org).map { schedule =>
      schedule.upcomingDates.foreach { date =>
        MeetingsDao.findAll(
          org = Some(org),
          scheduledAt = Some(date),
          limit = 1
        ).headOption.getOrElse {
          println(s" -- scheduling org[${org.key}] meeting for $date")
          MeetingsDao.create(
            User.Actor,
            FullMeetingForm(
              org,
              MeetingForm(
                scheduledAt = date
              )
            )
          )
        }
      }
    }
  }

  private[actors] def syncMeetingIncidents() {
    val limit = 100
    var offset = 0
    var haveMore = true
    while (haveMore) {
      val meetings = MeetingsDao.findAll(
        isUpcoming = Some(false),
        scheduledWithinNHours = Some(12),
        limit = limit,
        offset = 0
      )

      offset += 1
      haveMore = meetings.size >= limit
      meetings.foreach { meeting =>
        syncIncidentsForMeeting(meeting)
      }
    }
  }

  /**
    * Given a meeting, creates a SyncIncident message for each
    * incident in that meeting. This provides an easy way to
    * recalculate next steps for any incident in this meeting.
    */
  private[actors] def syncIncidentsForMeeting(meeting: Meeting) {
    val limit = 100
    var offset = 0
    var haveMore = true
    while (haveMore) {
      val incidents = IncidentsDao.findAll(
        meetingId = Some(meeting.id),
        limit = limit,
        offset = 0
      )

      offset += 1
      haveMore = incidents.size >= limit
      incidents.foreach { incident =>
        global.Actors.mainActor ! actors.MeetingMessage.SyncIncident(incident.id)
      }
    }
  }

  private[actors] def nextTask(incident: Incident): Option[Task] = {
    val incidentTasks = AgendaItemsDao.findAll(
      incidentId = Some(incident.id)
    ).map(_.task)

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
  private[actors] def assignIncident(incidentId: Long) {
    IncidentsDao.findById(incidentId).map { incident =>
      nextTask(incident).map { task =>
        // Only assign incidents for organizations w/ meetings
        // enabled. These orgs will always have upcoming meetings
        // (managed by the EnsureUpcoming message above)
        bestNextMeetingForOrg(incident.organization).map { meeting =>
          upsertIncidentInMeeting(meeting, incident, task)
        }
      }
    }
  }

  private[actors] def upsertIncidentInMeeting(
    meeting: Meeting,
    incident: Incident,
    task: Task
  ) {
    AgendaItemsDao.findAll(
      meetingId = Some(meeting.id),
      incidentId = Some(incident.id),
      limit = 1
    ).headOption.getOrElse {
      println("Creating agenda item for incident " + incident.id)
      AgendaItemsDao.create(
        User.Actor,
        FullAgendaItemForm(
          meeting,
          AgendaItemForm(
            incidentId = incident.id,
            task = task
          )
        )
      )
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

