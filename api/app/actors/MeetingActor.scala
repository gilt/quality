package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.IncidentsDao
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object MeetingMessage {
  case object SyncOrganizationMeetings
  case object SyncIncidents
  case object SyncMeetings
  case class SyncIncident(incidentId: Long)
}

class MeetingActor extends Actor {

  def receive = {

    case MainActor.AgendaItemCreated(agendaItemId: Long) => {
      println(s"MeetingActor MainActor.AgendaItemCreated($agendaItemId)")
      try {
        AgendaItemEvents.processCreated(agendaItemId)
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.AgendaItemCreated($agendaItemId): ${t}" , t)
      }
    }

    /**
      * Creates upcoming meetings for all organizations.
      */
    case MeetingMessage.SyncOrganizationMeetings => {
      try {
        Database.ensureAllOrganizationHaveUpcomingMeetings()
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.SyncOrganizationMeetings: ${t}" , t)
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
        case t: Throwable => Logger.error(s"MeetingMessage.SyncMeetings: ${t}" , t)
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
        case t: Throwable => Logger.error(s"MeetingMessage.SyncIncident($incidentId): ${t}" , t)
      }
    }

    case MeetingMessage.SyncIncidents => {
      try {
        IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
          sender ! MeetingMessage.SyncIncident(incidentId)
        }
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.SyncIncidents: ${t}" , t)
      }
    }

  }

}

