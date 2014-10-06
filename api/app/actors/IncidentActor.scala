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

      // TODO: Also have to look at incidents involved in meetings
      // that recently passed. This would catch the use case of
      //  - incident created, assigned to meeting
      //  - reviewed in meeting but incident record not actually modified
      //  - incident needs to get scheduled for next task in next meeting
      IncidentsDao.findRecentlyModifiedIncidentIds.foreach { incidentId =>
        sender ! IncidentMessage.SyncOne(incidentId)
      }
    }
  }

}

