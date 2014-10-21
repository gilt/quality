package controllers

import client.Api
import com.gilt.quality.models.{Publication, Subscription}
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
      case Publication.Incidentscreate => "Email me whenever an incident is created"
      case Publication.Incidentsupdate => "Email me whenever an incident is updated"
      case Publication.Planscreate => "Email me whenever a plan is created"
      case Publication.Plansupdate => "Email me whenever a plan is updated"
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
        limit = Some(Publication.all.size + 1)
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

}
