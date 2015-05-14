package controllers

import client.Api
import com.gilt.quality.v0.errors.UnitResponse

import play.api._
import play.api.mvc._
import play.api.data._

object EmailsController extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def meetingAdjourned(
    org: String,
    meetingId: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.emailMessages.getMeetingAdjournedByOrgAndMeetingId(org, meetingId).map { email =>
      Ok(views.html.emails.show(request.mainTemplate(), email))
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Meetings.index(org)).flashing("warning" -> "Meeting not found")
      }
    }
  }

}
