package actors

import com.gilt.quality.models.{Organization, Publication, Subscription}
import db.{Pager, SubscriptionsDao}
import lib.{Email, Person}
import akka.actor._
import play.api.Logger
import play.api.Play.current

private[actors] object Emails {

  def deliver(
    org: Organization,
    publication: Publication,
    subject: String,
    body: String
  ) {
    Pager.eachPage[Subscription] { offset =>
      SubscriptionsDao.findAll(
        organizationKey = Some(org.key),
        publication = Some(publication),
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
