package actors

import db.IncidentsDao
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Play.current

object IncidentMessage {
  case object SyncAll
  case class SyncOne(incidentId: Long)
}

class IncidentActor extends Actor {

  def receive = {
    case IncidentMessage.SyncAll => {
      println("IncidentMessage.SyncAll")
      IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
        sender ! IncidentMessage.SyncOne(incidentId)
      }
    }
  }

}

