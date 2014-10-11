package controllers

import client.Api
import com.gilt.quality.models.Organization
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import play.api.mvc._
import play.api.mvc.Results.Redirect
import play.api.Play.current

class OrgRequest[A](
  val org: Organization,
  request: Request[A]
) extends WrappedRequest[A](request)

object OrgAction extends ActionBuilder[OrgRequest] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def invokeBlock[A](request: Request[A], block: (OrgRequest[A]) => Future[Result]) = {

    request.path.split("/").drop(1).headOption match {
      case None => {
        Future.successful(Redirect("/").flashing("warning" -> s"No org key for request path[${request.path}]"))
      }

      case Some(orgKey) => {

        Await.result(Api.instance.Organizations.getByKey(orgKey), 1000.millis).headOption match {
          case None => {
            Future.successful(Redirect("/").flashing("warning" -> s"Organization $orgKey not found"))
          }

          case Some(org: Organization) => {
            block(new OrgRequest(org, request))
          }
        }
      }
    }

  }
}
