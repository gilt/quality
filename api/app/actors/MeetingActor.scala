package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.IncidentsDao
import akka.actor._
import play.api.Play.current

object MeetingMessage {
  case object SyncMeetings
  case object SyncIncidents
  case object SyncMeetingIncidents
  case class SyncIncident(incidentId: Long)
}

class MeetingActor extends Actor {

  def receive = {

    /**
      * Creates upcoming meetings for all organizations.
      */
    case MeetingMessage.SyncMeetings => {
      println("MeetingMessage.SyncMeetings")
      Database.ensureAllOrganizationHaveUpcomingMeetings()
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
    case MeetingMessage.SyncMeetingIncidents => {
      println("MeetingMessage.SyncMeetingIncidents")
      Database.syncMeetingIncidents()
    }

    /**
      * Triggered whenever an incident is created or updated.
      * Makes sure that:
      * 
      *  a. This incident is assigned to an upcoming meeting
      *  b. OR this incident has already been in a meeting for all Tasks
      */
    case MeetingMessage.SyncIncident(incidentId) => {
      println(s"MeetingMessage.SyncIncident($incidentId)")
      Database.assignIncident(incidentId)
    }

    case MeetingMessage.SyncIncidents => {
      println("MeetingMessage.SyncIncidents")

      IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
        sender ! MeetingMessage.SyncIncident(incidentId)
      }
    }

  }

}

