package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.IncidentsDao
import akka.actor._
import play.api.Play.current

object MeetingMessage {
  case object SyncMeetings
  case object SyncIncidents
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

      // TODO: Also have to look at incidents involved in meetings
      // that recently passed. This would catch the use case of
      //  - incident created, assigned to meeting
      //  - reviewed in meeting but incident record not actually modified
      //  - incident needs to get scheduled for next task in next meeting
      IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
        sender ! MeetingMessage.SyncIncident(incidentId)
      }
    }

  }

}

