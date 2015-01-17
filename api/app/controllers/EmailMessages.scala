package controllers

import com.gilt.quality.v0.models.{EmailMessage, Meeting}
import com.gilt.quality.v0.models.json._
import actors.MeetingAdjournedEmail
import db.MeetingsDao

import play.api.mvc._
import play.api.libs.json._

object EmailMessages extends Controller {

  def getMeetingAdjournedByOrgAndMeetingId(
    org: String,
    meetingId: Long
  ) = OrgAction { request =>
    val user = db.UsersDao.findByEmail("michael@gilt.com").getOrElse(request.user)
    MeetingsDao.findByOrganizationAndId(request.org, meetingId) match {
      case None => NotFound
      case Some(meeting) => {
        val email = MeetingAdjournedEmail(meeting.id).email(user)
        Ok(Json.toJson(email))
      }
    }
  }

}
