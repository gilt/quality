package controllers

import com.gilt.quality.models.{Meeting, Organization, User}
import db.{MeetingsDao, OrganizationsDao, UsersDao}
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
            block(new OrgRequest(UsersDao.Default, org, request))
          }
        }
      }
    }

  }
}



class MeetingRequest[A](
  val user: User,
  val org: Organization,
  val meeting: Meeting,
  request: Request[A]
) extends WrappedRequest[A](request)

object MeetingAction extends ActionBuilder[MeetingRequest] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def invokeBlock[A](request: Request[A], block: (MeetingRequest[A]) => Future[Result]) = {

    val parts = request.path.split("/").drop(1).toList
    parts.headOption match {
      case None => {
        Future.successful(Redirect("/").flashing("warning" -> s"No org key for request path[${request.path}]"))
      }

      case Some(orgKey) => {
        OrganizationsDao.findByKey(orgKey) match {
          case None => {
            Future.successful(Redirect("/").flashing("warning" -> s"Organization $orgKey not found"))
          }

          case Some(org: Organization) => {
            if (parts.size >= 3 && parts(1) == "meetings" && !parts(2).isEmpty) {
              val meetingId = parts(2).toLong
              MeetingsDao.findByOrganizationAndId(org, meetingId) match {
                case None => {
                  Future.successful(Redirect("/").flashing("warning" -> s"Meeting $meetingId not found"))
                }
                case Some(meeting) => {
                  block(new MeetingRequest(UsersDao.Default, org, meeting, request))
                }
              }
            } else {
              Future.successful(Redirect("/").flashing("warning" -> s"Missing meeting_id in request path"))
            }
          }
        }
      }
    }

  }
}
