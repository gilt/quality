package controllers

import client.Api
import com.gilt.quality.models.{Organization, User}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import play.api.mvc._
import play.api.mvc.Results.Redirect
import play.api.Play.current
import java.util.UUID

case class RequestHelper[A](request: Request[A]) {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org: Option[Organization] = {
    request.path.split("/").drop(1).headOption.flatMap { orgKey =>
      Await.result(Api.instance.organizations.getByKey(orgKey), 1000.millis).headOption
    }
  }

  lazy val user: Option[User] = {
    request.session.get("user_guid").flatMap { userGuid =>
      Await.result(Api.instance.users.getByGuid(UUID.fromString(userGuid)), 1000.millis)
    }
  }

}

class OrgRequest[A](
  val user: User,
  val org: Organization,
  request: Request[A]
) extends WrappedRequest[A](request) {

  def mainTemplate(
    title: Option[String] = None
  ) = lib.MainTemplate(
    user = Some(user),
    org = Some(org),
    title = title
  )

}

object OrgAction extends ActionBuilder[OrgRequest] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def invokeBlock[A](request: Request[A], block: (OrgRequest[A]) => Future[Result]) = {

    val helper = RequestHelper(request)
    helper.user match {
      case None => {
        Future.successful(Redirect(routes.LoginController.index(return_url = Some(request.path))).flashing("warning" -> s"Please login"))
      }
      case Some(user) => {
        helper.org match {
          case None => {
            Future.successful(Redirect("/").flashing("warning" -> s"No org for request path[${request.path}]"))
          }
          case Some(org) => {
            block(new OrgRequest(user, org, request))
          }
        }
      }
    }
  }
}


class AuthenticatedRequest[A](
  val user: User,
  request: Request[A]
) extends WrappedRequest[A](request) {

  def mainTemplate(
    title: Option[String] = None
  ) = lib.MainTemplate(
    user = Some(user),
    title = title
  )

}


object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {

    val helper = RequestHelper(request)
    helper.user match {
      case None => {
        Future.successful(Redirect(routes.LoginController.index(return_url = Some(request.path))).flashing("warning" -> s"Please login"))
      }
      case Some(user) => {
        block(new AuthenticatedRequest(user, request))
      }
    }
  }
}
