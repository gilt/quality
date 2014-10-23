package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.IncidentsDao
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object MeetingMessage {
  case object EnsureUpcomingMeetings
  case object SyncIncidents
  case class SyncIncident(incidentId: Long)
  case object AutoAdjournMeetings
  case class SyncMeeting(meetingId: Long)
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
    case MeetingMessage.EnsureUpcomingMeetings => {
      try {
        Database.ensureAllOrganizationHaveUpcomingMeetings()
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.EnsureUpcomingMeetings: ${t}" , t)
      }
    }

    /**
      * Looks at meetings that have recently ended but are not
      * adjourned and automatically adjourns them. This in turn
      * triggers the SyncMeeting event for each meeting that is
      * adjourned, allowing the workflow to continue for all incidents
      * in the meeting that are not yet done.
      */
    case MeetingMessage.AutoAdjournMeetings => {
      try {
        Database.autoAdjournMeetings()
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.AutoAdjournMeetings: ${t}" , t)
      }
    }

    /**
      * Looks at incidents assigned to this meeting. Those incidents
      * might not have been updated, so we trigger a sync on those
      * incidents here. Normal use case is to trigger this event
      * whenever a meeting is adjourned.
      * 
      * This catches the use case of
      *   - incident created, assigned to meeting
      *   - reviewed in meeting but incident record not actually modified
      *   - incident needs to get scheduled for next task in next meeting
      */
    case MeetingMessage.SyncMeeting(meetingId) => {
      try {
        Database.syncMeetingById(meetingId)
      } catch {
        case t: Throwable => Logger.error(s"MeetingMessage.SyncMeeting($meetingId): ${t}" , t)
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

