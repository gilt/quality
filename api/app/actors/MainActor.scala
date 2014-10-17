package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Play.current

object MainActor {
  def props() = Props(new MainActor("main"))
}

class MainActor(name: String) extends Actor with ActorLogging {
  import scala.concurrent.duration._

  val meetingActor = Akka.system.actorOf(Props[MeetingActor], name = s"$name:meetingActor")

  Akka.system.scheduler.schedule(15.seconds, 1.minutes, meetingActor, InternalMeetingMessage.SyncOrganizationMeetings)
  Akka.system.scheduler.schedule(20.seconds, 1.minutes, meetingActor, InternalMeetingMessage.SyncMeetings)
  Akka.system.scheduler.schedule(25.seconds, 15.minutes, meetingActor, InternalMeetingMessage.SyncIncidents)

  def receive = akka.event.LoggingReceive {
    case e: MeetingMessage.IncidentCreated(incidentId) => {
      println(s"MainActor: Received MeetingMessage.IncidentCreated($incidentId)")
      meetingActor ! e
    }

    case MeetingMessage.IncidentUpdated(incidentId) => {
      println(s"MainActor: Received MeetingMessage.IncidentUpdated($incidentId)")
      meetingActor ! InternalMeetingMessage.SyncIncident(incidentId)
    }

    case MeetingMessage.IncidentTeamUpdated(incidentId) => {
      println(s"MainActor: Received MeetingMessage.IncidentTeamUpdated($incidentId)")
      // TODO: Send Email to the team if this incident is an upcoming meeting
    }

    case MeetingMessage.AgendaItemCreated(agendaItemId) => {
      println(s"MainActor: Received MeetingMessage.AgendaItemCreated($agendaItemId)")
      meetingActor ! MeetingMessage.AgendaItemCreated(agendaItemId)
    }

    case InternalMeetingMessage.SyncIncident(incidentId) => {
      println(s"MainActor: Received InternalMeetingMessage.SyncIncident($incidentId)")
      meetingActor ! InternalMeetingMessage.SyncIncident(incidentId)
    }

    case m: Any => {
      println("Main actor got an unhandled message: " + m)
    }
  }
}
