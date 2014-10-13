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

  Akka.system.scheduler.schedule(15.seconds, 1.minutes, meetingActor, MeetingMessage.SyncOrganizationMeetings)
  Akka.system.scheduler.schedule(20.seconds, 1.minutes, meetingActor, MeetingMessage.SyncMeetings)
  Akka.system.scheduler.schedule(25.seconds, 15.minutes, meetingActor, MeetingMessage.SyncIncidents)

  def receive = akka.event.LoggingReceive {
    case MeetingMessage.SyncIncident(incidentId) => {
      println(s"MainActor: IncidentMessage.SyncOne($incidentId)")
      meetingActor ! MeetingMessage.SyncIncident(incidentId)
    }
    case m: Any => {
      println("Main actor got a messages: " + m)
    }
  }
}
