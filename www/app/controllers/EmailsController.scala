package controllers

import client.Api

import play.api._
import play.api.mvc._
import play.api.data._

object EmailsController extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def meetingAdjourned(
    org: String,
    meetingId: Long
  ) = OrgAction.async { implicit request =>
    for {
      em <- Api.instance.emailMessages.getEmailMessagesAndMeetingAdjournedByOrgAndMeetingId(org, meetingId)
    } yield {
      em match {
        case None => {
          Redirect(routes.Meetings.index(org)).flashing("warning" -> "Meeting not found")
        }
        case Some(email) => {
          Ok(views.html.emails.show(request.mainTemplate(), email))
        }
      }
    }
  }

}
