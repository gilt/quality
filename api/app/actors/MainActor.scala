package actors

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Play.current

object MainActor {
  def props() = Props(new MainActor)
}

class MainActor extends Actor with ActorLogging {
  import scala.concurrent.duration._

  val meetingActor = Akka.system.actorOf(Props[MeetingActor], name = "meetingActor")

  Akka.system.scheduler.schedule(0.seconds, 1.minutes, meetingActor, MeetingMessage.SyncMeetings)
  Akka.system.scheduler.schedule(30.seconds, 60.minutes, meetingActor, MeetingMessage.SyncIncidents)

  // TODO: For local testing only
  Akka.system.scheduler.schedule(1.seconds, 10.seconds, meetingActor, MeetingMessage.SyncIncident(685))

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
