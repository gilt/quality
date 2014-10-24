package actors

import com.gilt.quality.models.{Publication, Subscription}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.{IncidentsDao, Pager, PlansDao, SubscriptionsDao}
import lib.{Email, Person}
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object EmailMessage {
  case class Incident(publication: Publication, incidentId: Long)
  case class IncidentTeamUpdated(publication: Publication, incidentId: Long)
  case class Plan(publication: Publication, planId: Long)
}

class EmailActor extends Actor {

  def receive = {

    case EmailMessage.Incident(publication: Publication, incidentId: Long) => {
      println(s"EmailActor EmailMessage.Incident($publication, $incidentId)")
      try {
        IncidentsDao.findById(incidentId).map { incident =>
          Emails.deliverToAllSubscribers(
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

    /**
      * Notify all team members that an incident has been assigned to their team.
      */
    case EmailMessage.IncidentTeamUpdated(publication: Publication, incidentId: Long) => {
      println(s"EmailActor EmailMessage.IncidentTeamUpdated($publication, $incidentId)")
      try {
        IncidentsDao.findById(incidentId).map { incident =>
          incident.team match {
            case None => {
              // Might indicate team was already removed from the incident
              Logger.warn(s"EmailMessage.IncidentTeamUpdated($publication, $incidentId): No team found for incident")
            }
            case Some(team) => {
              val subject = s"[PerfectDay] Incident ${incident.id} Assigned to Team ${team.key}"
              val body = views.html.emails.incident(Emails.qualityWebHostname, incident).toString

              Pager.eachPage[Subscription] { offset =>
                SubscriptionsDao.findAll(
                  organizationKey = Some(team.organization.key),
                  publication = Some(publication),
                  team = Some(team),
                  limit = 100,
                  offset = offset
                )
              } { subscription =>
                Logger.info(s"Emails: delivering email for subscription[$subscription]")
                Email.sendHtml(
                  to = Person(email = subscription.user.email),
                  subject = subject,
                  body = body
                )
              }
            }
          }
        }
      } catch {
        case t: Throwable => Logger.error(s"EmailMessage.IncidentTeamUpdated($publication, $incidentId): ${t}" , t)
      }
    }

    case EmailMessage.Plan(publication: Publication, planId: Long) => {
      println(s"EmailActor EmailMessage.Plan($publication, $planId)")
      try {
        PlansDao.findById(planId).map { plan =>
          IncidentsDao.findById(plan.incidentId).map { incident =>
            Emails.deliverToAllSubscribers(
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
