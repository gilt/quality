package controllers

import com.gilt.quality.models.{Error, Meeting, MeetingForm}
import com.gilt.quality.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullMeetingForm, MeetingsDao, OrganizationsDao, User}
import lib.Validation

object Meetings extends Controller {

  val orgKey = "gilt" // TODO
  lazy val org = OrganizationsDao.findByKey(orgKey).get // TODO

  def get(
    id: Option[Long],
    limit: Int = 25,
    offset: Int = 0
  ) = Action { request =>
    val meetings = MeetingsDao.findAll(
      id = id,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(meetings))
  }

  def getById(id: Long) = Action {
    MeetingsDao.findByOrganizationAndId(org, id) match {
      case None => NotFound
      case Some(m: Meeting) => Ok(Json.toJson(m))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[MeetingForm] match {
      case e: JsError => Conflict(Json.toJson(Validation.invalidJson(e)))

      case s: JsSuccess[MeetingForm] => {
        val form = FullMeetingForm(org, s.get)
        val meeting = MeetingsDao.create(User.Default, form)
        Created(Json.toJson(meeting)).withHeaders(LOCATION -> routes.Meetings.getById(meeting.id).url)
      }
    }
  }

  def deleteById(id: Long) = Action { request =>
    MeetingsDao.findByOrganizationAndId(org, id).foreach { m =>
      MeetingsDao.softDelete(User.Default, m)
    }
    NoContent
  }

}
