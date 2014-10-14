package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.IncidentsDao
import akka.actor._
import play.api.Play.current

object MeetingMessage {
  case object SyncOrganizationMeetings
  case object SyncIncidents
  case object SyncMeetings
  case class SyncIncident(incidentId: Long)
  case class NewAgendaItem(agendaItemId: Long)
}

class MeetingActor extends Actor {

  def receive = {

    /**
      * Creates upcoming meetings for all organizations.
      */
    case MeetingMessage.SyncOrganizationMeetings => {
      try {
        Database.ensureAllOrganizationHaveUpcomingMeetings()
      } catch {
        case e: Throwable => println("ERROR: " + e)
      }
    }

    /**
      * Looks at incidents assigned to meeting that recently
      * passed. Those incidents might not have been updated, so we
      * trigger a sync on those incidents here.
      * 
      * This catches the use case of
      *   - incident created, assigned to meeting
      *   - reviewed in meeting but incident record not actually modified
      *   - incident needs to get scheduled for next task in next meeting
      */
    case MeetingMessage.SyncMeetings => {
      try {
        Database.syncMeetings()
      } catch {
        case e: Throwable => println("ERROR: " + e)
      }
    }

    /**
      * Triggered whenever an incident is created or updated.
      * Makes sure that:
      * 
      *  a. This incident is assigned to an upcoming meeting
      *  b. OR this incident has already been in a meeting for all Tasks
      */
    case MeetingMessage.SyncIncident(incidentId) => {
      try {
        Database.assignIncident(incidentId)
      } catch {
        case e: Throwable => println("ERROR: " + e)
      }
    }

    /**
      * Notifies the team that they have an incident assigned to an
      *  upcoming meeting.
      */
    case MeetingMessage.NewAgendaItem(agendaItemId: Long) => {

    }

    case MeetingMessage.SyncIncidents => {
      IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
        sender ! MeetingMessage.SyncIncident(incidentId)
      }
    }

  }

}

