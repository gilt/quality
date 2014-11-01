package actors

import com.gilt.quality.models.{EmailMessage, Publication, Subscription}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.{IncidentsDao, Pager, PlansDao, SubscriptionsDao}
import lib.{Email, Person}
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object EmailActorMessage {
  case class Incident(publication: Publication, incidentId: Long)
  case class IncidentTeamUpdated(publication: Publication, incidentId: Long)
  case class Plan(publication: Publication, planId: Long)
  case class MeetingAdjourned(meetingId: Long)
}

class EmailActor extends Actor {

  def receive = {

    case EmailActorMessage.Incident(publication: Publication, incidentId: Long) => {
      withErrorHandler(s"EmailActor EmailActorMessage.Incident($publication, $incidentId)", {
        IncidentsDao.findById(incidentId).map { incident =>
          Emails.deliver(
            org = incident.organization,
            publication = publication,
            subject = s"Incident ${incident.id} #{incident.summary} ${Emails.action(publication)}",
            body = views.html.emails.incident(incident).toString
          )
        }
      })
    }

    /**
      * Notify all team members that an incident has been assigned to their team.
      */
    case EmailActorMessage.IncidentTeamUpdated(publication: Publication, incidentId: Long) => {
      withErrorHandler(s"EmailActor EmailActorMessage.IncidentTeamUpdated($publication, $incidentId)", {
        IncidentsDao.findById(incidentId).map { incident =>
          incident.team match {
            case None => {
              // Might indicate team was already removed from the
              // incident in which case we do not send the email
              // notification
              Logger.warn(s"EmailActorMessage.IncidentTeamUpdated($publication, $incidentId): No team found for incident")
            }
            case Some(team) => {
              Emails.deliver(
                org = incident.organization,
                publication = publication,
                subject = s"Incident ${incident.id} Assigned to Team ${team.key}",
                body = views.html.emails.incident(incident).toString,
                team = Some(team)
              )
            }
          }
        }
      })
    }

    case EmailActorMessage.Plan(publication: Publication, planId: Long) => {
      withErrorHandler(s"EmailActor EmailActorMessage.Plan($publication, $planId)", {
        PlansDao.findById(planId).map { plan =>
          IncidentsDao.findById(plan.incidentId).map { incident =>
            Emails.deliver(
              org = incident.organization,
              publication = publication,
              subject = s"Incident ${incident.id} Plan ${Emails.action(publication)}",
              body = views.html.emails.incident(incident).toString
            )
          }
        }
      })
    }

    case EmailActorMessage.MeetingAdjourned(meetingId: Long) => {
      withErrorHandler(s"EmailActor EmailActorMessage.MeetingAdjourned($meetingId)", {
        MeetingAdjournedEmail(meetingId).send()
      })
    }

    case m: Any => {
      Logger.error("EmailActor got an unhandled message: " + m)
    }

  }

  private def withErrorHandler(
    description: String,
    f: => Any
  ) {
    println(description)
    try {
      f
    } catch {
      case t: Throwable => Logger.error(s"$description: ${t}" , t)
    }
  }

}
