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
    }

    case IncidentMessage.SyncOne(incidentId) => {
      println(s"IncidentActor IncidentMessage.SyncOne($incidentId)")
      syncIncidentById(incidentId)
    }
  }

  private def syncIncidentById(incidentId: Long) {
    IncidentsDao.findById(incidentId) match {
      case None => {
        // No-op
      }
      case Some(incident) => {

      }
    }
  }
}

