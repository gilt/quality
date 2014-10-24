package controllers

import client.Api
import com.gilt.quality.models.{Organization, User, Team}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import play.api.mvc._
import play.api.mvc.Results.Redirect
import play.api.Play.current
import java.util.UUID

case class RequestHelper[A](request: Request[A]) {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val pathParts = request.path.split("/").drop(1)

  lazy val org: Option[Organization] = {
    team match {
      case None => {
        pathParts.headOption.flatMap { orgKey =>
          Await.result(Api.instance.organizations.getByKey(orgKey), 1000.millis).headOption
        }
      }
      case Some(t) => {
        Some(t.organization)
      }
    }
  }

  lazy val team: Option[Team] = {
    val orgKey = pathParts.headOption
    if (pathParts.length >= 3 && pathParts(1) == "teams") {
      val teamKey = pathParts(2)
      Await.result(Api.instance.teams.getByOrgAndKey(orgKey.get, teamKey), 1000.millis).headOption
    } else {
      None
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

class TeamRequest[A](
  val user: User,
  val team: Team,
  request: Request[A]
) extends WrappedRequest[A](request) {

  val org = team.organization

  def mainTemplate(
    title: Option[String] = None
  ) = lib.MainTemplate(
    user = Some(user),
    org = Some(org),
    title = title
  )

}

object TeamAction extends ActionBuilder[TeamRequest] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def invokeBlock[A](request: Request[A], block: (TeamRequest[A]) => Future[Result]) = {

    val helper = RequestHelper(request)
    helper.user match {
      case None => {
        Future.successful(Redirect(routes.LoginController.index(return_url = Some(request.path))).flashing("warning" -> s"Please login"))
      }
      case Some(user) => {
        helper.team match {
          case None => {
            Future.successful(Redirect("/").flashing("warning" -> s"No team for request path[${request.path}]"))
          }
          case Some(team) => {
            block(new TeamRequest(user, team, request))
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
