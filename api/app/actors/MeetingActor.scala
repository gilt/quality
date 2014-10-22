package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.IncidentsDao
import akka.actor._
import play.api.Logger
import play.api.Play.current

object MeetingMessage {
  case class IncidentCreated(incidentId: Long)
  case class IncidentUpdated(incidentId: Long)
  case class IncidentTeamUpdated(incidentId: Long)
  case class AgendaItemCreated(agendaItemId: Long)
  case class PlanCreated(planId: Long)
  case class PlanUpdated(planId: Long)
}

private[actors] object InternalMeetingMessage {
  case object SyncOrganizationMeetings
  case object SyncIncidents
  case object SyncMeetings
  case class SyncIncident(incidentId: Long)
}

class MeetingActor extends Actor {

  def receive = {

    case MeetingMessage.AgendaItemCreated(agendaItemId: Long) => {
      println(s"MeetingActor MeetingMessage.AgendaItemCreated($agendaItemId)")
      try {
        AgendaItemEvents.processCreated(agendaItemId)
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.AgendaItemCreated($agendaItemId): ${t}" , t)
      }
    }

    /**
      * Creates upcoming meetings for all organizations.
      */
    case InternalMeetingMessage.SyncOrganizationMeetings => {
      try {
        Database.ensureAllOrganizationHaveUpcomingMeetings()
      } catch {
        case t: Throwable => Logger.error(s"InternalMeetingMessage.SyncOrganizationMeetings: ${t}" , t)
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
    case InternalMeetingMessage.SyncMeetings => {
      try {
        Database.syncMeetings()
      } catch {
        case t: Throwable => Logger.error(s"InternalMeetingMessage.SyncMeetings: ${t}" , t)
      }
    }

    /**
      * Triggered whenever an incident is created or updated.
      * Makes sure that:
      * 
      *  a. This incident is assigned to an upcoming meeting
      *  b. OR this incident has already been in a meeting for all Tasks
      */
    case InternalMeetingMessage.SyncIncident(incidentId) => {
      try {
        Database.assignIncident(incidentId)
      } catch {
        case t: Throwable => Logger.error(s"InternalMeetingMessage.SyncIncident($incidentId): ${t}" , t)
      }
    }

    case InternalMeetingMessage.SyncIncidents => {
      try {
        IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
          sender ! InternalMeetingMessage.SyncIncident(incidentId)
        }
      } catch {
        case t: Throwable => Logger.error(s"InternalMeetingMessage.SyncIncidents: ${t}" , t)
      }
    }

  }

}

