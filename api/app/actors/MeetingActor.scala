package actors

import com.gilt.quality.models.{AgendaItemForm, Meeting, MeetingForm, Organization, Task}
import db.{FullAgendaItemForm, AgendaItemsDao, IncidentsDao, FullMeetingForm, MeetingsDao, OrganizationsDao, User}
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
      println("MeetingMessage.EnsureUpcoming")

      OrganizationsDao.findAll().foreach { org =>
        MeetingSchedule.findByOrganization(org).map { schedule =>
          schedule.upcomingDates.foreach { date =>
            MeetingsDao.findAll(
              orgKey = Some(org.key),
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
    }

    /**
      * Triggered whenever an incident is updated. Makes sure that:
      * 
      *  a. This incident is assigned to an upcoming meeting
      *  b. OR this incident has already been in a meeting for all Tasks
      */
    case MeetingMessage.AssignIncident(incidentId) => {
      println(s"MeetingMessage.AssignIncident($incidentId)")

      IncidentsDao.findById(incidentId).map { incident =>

        // Only assign incidents for organizations w/ meetings
        // enabled. These orgs will always have upcoming meetings
        // (managed by the EnsureUpcoming message above)
        bestNextMeetingForOrg(incident.organization).map { meeting =>

          val incidentTasks = AgendaItemsDao.findAll(
            incidentId = Some(incidentId)
          ).map(_.task)

          AllTasks.find { t => !incidentTasks.contains(t) } match {
            case None => {
              // All tasks completed or scheduled
            }
            case Some(task) => {
              AgendaItemsDao.findAll(
                meetingId = Some(meeting.id),
                incidentId = Some(incidentId),
                limit = 1
              ).headOption match {
                case None => {
                  AgendaItemsDao.create(
                    User.Actor,
                    FullAgendaItemForm(
                      meeting,
                      AgendaItemForm(
                        incidentId = incidentId,
                        task = task
                      )
                    )
                  )
                }
                case Some(_) => {
                  // Incident already part of this next meeting
                }
              }
            }
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

