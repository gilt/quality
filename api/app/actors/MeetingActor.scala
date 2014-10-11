package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Play.current

object MeetingMessage {
  case object EnsureUpcoming
  case class AssignIncident(incidentId: Long)
}

class MeetingActor extends Actor {

  def receive = {

    /**
      * Creates upcoming meetings for all organizations.
      */
    case MeetingMessage.EnsureUpcoming => {
      println("MeetingMessage.EnsureUpcoming")
      Database.ensureAllOrganizationHaveUpcomingMeetings()
    }

    /**
      * Triggered whenever an incident is updated. Makes sure that:
      * 
      *  a. This incident is assigned to an upcoming meeting
      *  b. OR this incident has already been in a meeting for all Tasks
      */
    case MeetingMessage.AssignIncident(incidentId) => {
      println(s"MeetingMessage.AssignIncident($incidentId)")
      Database.assignIncident(incidentId)
    }

  }

}

