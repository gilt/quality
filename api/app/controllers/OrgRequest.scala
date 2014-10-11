package controllers

import com.gilt.quality.models.Organization
import db.{OrganizationsDao, User}
import scala.concurrent.Future
import play.api.mvc._
import play.api.mvc.Results.Redirect
import play.api.Play.current

class OrgRequest[A](
  val user: User,
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
        OrganizationsDao.findByKey(orgKey) match {
          case None => {
            Future.successful(Redirect("/").flashing("warning" -> s"Organization $orgKey not found"))
          }

          case Some(org: Organization) => {
            // TODO: Add authentication
            block(new OrgRequest(User.Default, org, request))
          }
        }
      }
    }

  }
}
