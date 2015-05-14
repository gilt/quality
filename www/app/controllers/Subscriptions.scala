package controllers

import client.Api
import com.gilt.quality.v0.models.{Publication, Subscription, SubscriptionForm}
import java.util.UUID
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object Subscriptions extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  case class UserPublication(publication: Publication, isSubscribed: Boolean) {
    val publicationName = publication match {
      case Publication.IncidentsCreate => "Email me whenever an incident is created"
      case Publication.IncidentsUpdate => "Email me whenever an incident is updated"
      case Publication.IncidentsTeamUpdate => "Email me whenever an incident is assigned to a team that I am on"
      case Publication.PlansCreate => "Email me whenever a plan is created"
      case Publication.PlansUpdate => "Email me whenever a plan is updated"
      case Publication.MeetingsAdjourned => "Email me a meeting summary whenever a meeting is adjourned"
      case Publication.UNDEFINED(key) => key
    }
  }

  def index(
    org: String
  ) = OrgAction.async { implicit request =>
    for {
      subscriptions <- Api.instance.subscriptions.get(
        organizationKey = Some(request.org.key),
        userGuid = Some(request.user.guid),
        limit = Publication.all.size + 1
      )
    } yield {
      val userPublications = Publication.all.map { p =>
        UserPublication(
          publication = p,
          isSubscribed = !subscriptions.find(_.publication == p).isEmpty
        )
      }
      Ok(views.html.subscriptions.index(request.mainTemplate(), userPublications))
    }
  }

  def postToggle(
    org: String,
    publication: Publication
  ) = OrgAction.async { implicit request =>
    for {
      subscriptions <- Api.instance.subscriptions.get(
        organizationKey = Some(request.org.key),
        userGuid = Some(request.user.guid),
        publication = Some(publication)
      )
    } yield {
      subscriptions.headOption match {
        case None => {
          Await.result(
            Api.instance.subscriptions.post(
              SubscriptionForm(
                organizationKey = request.org.key,
                userGuid = request.user.guid,
                publication = publication
              )
            ),
            1000.millis
          )
          Redirect(routes.Subscriptions.index(org)).flashing("success" -> "Subscription added")
        }
        case Some(subscription) => {
          Await.result(
            Api.instance.subscriptions.deleteById(subscription.id), 1000.millis
          )
          Redirect(routes.Subscriptions.index(org)).flashing("success" -> "Subscription removed")
        }
      }
    }

  }

}
