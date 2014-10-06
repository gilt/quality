package actors

import com.gilt.quality.models.{AgendaItemForm, Meeting, MeetingForm, Organization, Task}
import db.{AgendaItemFullForm, AgendaItemsDao, IncidentsDao, FullMeetingForm, MeetingsDao, OrganizationsDao, User}
import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Play.current

object MeetingMessage {
  case object EnsureUpcoming
  case class AssignIncident(incidentId: Long)
}

class MeetingActor extends Actor {

  val AllTasks = Seq(Task.ReviewTeam, Task.ReviewPlan)

  def receive = {

    /**
      * Creates upcoming meetings for all organizations.
      */
    case MeetingMessage.EnsureUpcoming => {
      println("MeetingMessage.EnsureUpcoming - TODO")

      OrganizationsDao.findAll().foreach { org =>
        MeetingSchedule.findByOrganization(org).map { schedule =>
          schedule.upcomingDates.foreach { date =>
            println(s" -- date[$date]")

            MeetingsDao.findAll(
              orgKey = Some(org.key),
              scheduledAt = Some(date),
              limit = 1
            ).headOption.getOrElse {
              println(s"  -- scheduling meeting for $date")
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
    }

    /**
      * Triggered whenever an incident is updated. Makes sure that:
      * 
      *  a. This incident is assigned to an upcoming meeting
      *  b. OR this incident has already been in a meeting for all Tasks
      */
    case MeetingMessage.AssignIncident(incidentId) => {
      println(s"MeetingMessage.AssignIncident($incidentId) - TODO")

      AllTasks.find { task =>
        AgendaItemsDao.findAll(
          incidentId = Some(incidentId),
          task = Some(task),
          limit = 1
        ).headOption.isEmpty
      }.map { task =>
        assignIncidentToNextMeeting(incidentId, task)
      }
    }

  }

  private def assignIncidentToNextMeeting(
    incidentId: Long,
    task: Task
  ) {
    IncidentsDao.findById(incidentId) match {
      case None => {
        // Incident already deleted
      }
      case Some(incident) => {
        bestNextMeetingForOrg(incident.organization) match {
          case None => {
            println(s" -- org[[${incident.organization.key}] - no upcoming meeting")
          }
          case Some(meeting) => {
            println(s" -- assigning[${incident.id}] for task[$task] to meeting[$meeting]")
            AgendaItemsDao.create(
              User.Actor,
              AgendaItemFullForm(
                meeting,
                AgendaItemForm(
                  incidentId = incident.id,
                  task = task
                )
              )
            )
          }
        }
      }
    }
  }

  private def bestNextMeetingForOrg(org: Organization): Option[Meeting] = {
    val twelveHoursFromNow = (new DateTime()).plus(3600*12*1000l)
    MeetingsDao.findAll(
      orgKey = Some(org.key),
      isUpcoming = Some(true)
    ).reverse.find(_.scheduledAt.isAfter(twelveHoursFromNow))
  }

}

