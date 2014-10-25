package actors

import com.gilt.quality.models.{Organization, Publication, Subscription, Task, Team}
import db.{Pager, SubscriptionsDao}
import lib.{Email, Person}
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object Emails {

  val qualityWebHostname = current.configuration.getString("quality.webHostname").getOrElse {
    sys.error(s"configuration parameter[quality.webHostname] is required")
  }

  def taskLabel(task: Task): String = {
    task match {
      case Task.ReviewTeam => "review team assignment"
      case Task.ReviewPlan => "review the prevention plan"
      case Task.UNDEFINED(key) => key
    }
  }

  def action(publication: Publication): String = {
    publication match {
      case Publication.IncidentsCreate | Publication.PlansCreate => "Created"
      case Publication.IncidentsUpdate | Publication.PlansUpdate => "Updated"
      case Publication.MeetingsAdjourned => "Adjourned"
      case Publication.IncidentsTeamUpdate => "Team Updated"
      case Publication.UNDEFINED(key) => key
    }
  }

  def deliver(
    org: Organization,
    publication: Publication,
    subject: String,
    body: String,
    team: Option[Team] = None
  ) {
    eachSubscription(org, publication, team, { subscription =>
      Logger.info(s"Emails: delivering email for subscription[$subscription]")
      Email.sendHtml(
        to = Person(email = subscription.user.email),
        subject = subject,
        body = body
      )
    })
  }

  def eachSubscription(
    organization: Organization,
    publication: Publication,
    team: Option[Team] = None,
    f: Subscription => Unit
  ) {
    Pager.eachPage[Subscription] { offset =>
      SubscriptionsDao.findAll(
        organizationKey = Some(organization.key),
        publication = Some(publication),
        team = team,
        limit = 100,
        offset = offset
      )
    } { subscription =>
      f(subscription)
    }
  }

}
