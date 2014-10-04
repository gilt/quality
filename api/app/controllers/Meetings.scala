package controllers

import com.gilt.quality.models.{Error, Meeting, MeetingForm}
import com.gilt.quality.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{MeetingsDao, User}
import lib.Validation

object Meetings extends Controller {

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
    MeetingsDao.findById(id) match {
      case None => NotFound
      case Some(m: Meeting) => Ok(Json.toJson(m))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[MeetingForm] match {
      case e: JsError => Conflict(Json.toJson(Validation.invalidJson(e)))

      case s: JsSuccess[MeetingForm] => {
        val meeting = MeetingsDao.create(User.Default, s.get)
        Created(Json.toJson(meeting)).withHeaders(LOCATION -> routes.Meetings.getById(meeting.id).url)
      }
    }
  }

  def deleteById(id: Long) = Action { request =>
    MeetingsDao.findById(id).foreach { m =>
      MeetingsDao.softDelete(User.Default, m)
    }
    NoContent
  }

}
