package actors

import com.gilt.quality.models.{Publication, Subscription}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.{IncidentsDao, Pager, PlansDao, SubscriptionsDao}
import lib.{Email, Person}
import akka.actor._
import play.api.Logger
import play.api.Play.current

case class EmailMessage(
  subject: String,
  body: String
)

private[actors] object EmailMessage {
  case class Incident(publication: Publication, incidentId: Long)
  case class IncidentTeamUpdated(publication: Publication, incidentId: Long)
  case class Plan(publication: Publication, planId: Long)
  case class MeetingAdjourned(meetingId: Long)
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

    /**
      * Notify all team members that an incident has been assigned to their team.
      */
    case EmailMessage.IncidentTeamUpdated(publication: Publication, incidentId: Long) => {
      println(s"EmailActor EmailMessage.IncidentTeamUpdated($publication, $incidentId)")
      try {
        IncidentsDao.findById(incidentId).map { incident =>
          incident.team match {
            case None => {
              // Might indicate team was already removed from the
              // incident in which case we do not send the email
              // notification
              Logger.warn(s"EmailMessage.IncidentTeamUpdated($publication, $incidentId): No team found for incident")
            }
            case Some(team) => {
              Emails.deliver(
                org = incident.organization,
                publication = publication,
                subject = s"[PerfectDay] Incident ${incident.id} Assigned to Team ${team.key}",
                body = views.html.emails.incident(Emails.qualityWebHostname, incident).toString,
                team = Some(team)
              )
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

    case EmailMessage.MeetingAdjourned(meetingId: Long) => {
      println(s"EmailActor EmailMessage.MeetingAdjourned($meetingId)")
      try {
        MeetingAdjournedEmail(meetingId).send()
      } catch {
        case t: Throwable => Logger.error(s"EmailMessage.MeetingAdjourned($meetingId): ${t}" , t)
      }
    }

    case m: Any => {
      Logger.error("EmailActor got an unhandled message: " + m)
    }

  }

}
