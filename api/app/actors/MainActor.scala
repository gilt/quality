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

  Akka.system.scheduler.schedule(0.seconds, 2.seconds, incidentActor, IncidentMessage.SyncAll)

  def receive = akka.event.LoggingReceive {
    case IncidentMessage.SyncOne(incidentId) => {
      println(s"MainActor: IncidentMessage.SyncOne($incidentId)")
      incidentActor ! IncidentMessage.SyncOne(incidentId)
    }
    case m: Any => {
      println("Main actor got a messages: " + m)
    }
  }
}
