package controllers

import com.gilt.quality.models.{EmailMessage, Meeting}
import com.gilt.quality.models.json._
import actors.MeetingAdjournedEmail
import db.MeetingsDao

import play.api.mvc._
import play.api.libs.json._

object EmailMessages extends Controller {

  def getMeetingAdjournedByOrgAndMeetingId(
    org: String,
    meetingId: Long
  ) = OrgAction { request =>
    MeetingsDao.findByOrganizationAndId(request.org, meetingId) match {
      case None => NotFound
      case Some(meeting) => {
        val email = MeetingAdjournedEmail(meeting.id).email
        Ok(Json.toJson(email))
      }
    }
  }

}
