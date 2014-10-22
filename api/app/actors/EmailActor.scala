package actors

import com.gilt.quality.models.Publication
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.{IncidentsDao, PlansDao}
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object EmailMessage {
  case class Incident(publication: Publication, incidentId: Long)
  case class Plan(publication: Publication, planId: Long)
}

class EmailActor extends Actor {

  def receive = {

    case EmailMessage.Incident(publication: Publication, incidentId: Long) => {
      println(s"EmailActor EmailMessage.Incident($publication, $incidentId)")
      try {
        IncidentsDao.findById(incidentId).map { incident =>
          Emails.deliver(
            org = incident.organization,
            publication = publication,
            subject = s"[PerfectDay] Incident ${incident.id} ${Emails.action(publication)}",
            body = views.html.emails.incident(Emails.qualityWebHostname, incident).toString
          )
        }
      } catch {
        case t: Throwable => Logger.error(s"EmailMessage.Incident($publication, $incidentId): ${t}" , t)
      }
    }

    case EmailMessage.Plan(publication: Publication, planId: Long) => {
      println(s"EmailActor EmailMessage.Plan($publication, $planId)")
      try {
        PlansDao.findById(planId).map { plan =>
          IncidentsDao.findById(plan.incidentId).map { incident =>
            Emails.deliver(
              org = incident.organization,
              publication = publication,
              subject = s"[PerfectDay] Incident ${incident.id} Plan ${Emails.action(publication)}",
              body = views.html.emails.incident(Emails.qualityWebHostname, incident).toString
            )
          }
        }
      } catch {
        case t: Throwable => Logger.error(s"EmailMessage.Plan($publication, $planId): ${t}" , t)
      }
    }

    case m: Any => {
      Logger.error("EmailActor got an unhandled message: " + m)
    }

  }

}
