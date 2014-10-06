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

  val incidentActor = Akka.system.actorOf(Props[IncidentActor], name = "incidentActor")
  val meetingActor = Akka.system.actorOf(Props[MeetingActor], name = "meetingActor")

  Akka.system.scheduler.schedule(0.seconds, 1.minutes, meetingActor, MeetingMessage.EnsureUpcoming)
  Akka.system.scheduler.schedule(30.seconds, 60.minutes, incidentActor, IncidentMessage.SyncAll)

  // TODO: For local testing only
  Akka.system.scheduler.schedule(1.seconds, 10.seconds, meetingActor, MeetingMessage.AssignIncident(685))

  def receive = akka.event.LoggingReceive {
    case IncidentMessage.SyncOne(incidentId) => {
      println(s"MainActor: IncidentMessage.SyncOne($incidentId)")
      meetingActor ! MeetingMessage.AssignIncident(incidentId)
    }
    case m: Any => {
      println("Main actor got a messages: " + m)
    }
  }
}
