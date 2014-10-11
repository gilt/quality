package controllers

import db.{AgendaItemsDao, FullAgendaItemForm, MeetingsDao, User}
import com.gilt.quality.models.AgendaItemForm
import lib.Validation
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object AgendaItems extends Controller with AgendaItems

trait AgendaItems {
  this: Controller =>

  def getMeetingsByOrgAndMeetingId(
    org: String,
    meetingId: Long,
    id: Option[Long],
    task: Option[com.gilt.quality.models.Task],
    limit: Int = 25,
    offset: Int = 0
  ) = MeetingAction { request =>
    val items = AgendaItemsDao.findAll(
      meetingId = Some(request.meeting.id),
      id = id,
      task = task,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(items))
  }

  def getMeetingsByOrgAndMeetingIdAndId(
    org: String,
    meetingId: Long,
    id: Long
  ) = MeetingAction { request =>
    AgendaItemsDao.findByMeetingIdAndId(request.meeting.id, id) match {
      case None => NotFound
      case Some(item) => Ok(Json.toJson(item))
    }
  }

  def deleteMeetingsByOrgAndMeetingIdAndId(
    org: String,
    meetingId: Long,
    id: Long
  ) = MeetingAction { request =>
    AgendaItemsDao.findByMeetingIdAndId(request.meeting.id, id).map { item =>
      AgendaItemsDao.softDelete(request.user, item)
    }
    NoContent
  }

  def postMeetingsByOrgAndMeetingId(
    org: String,
    meetingId: Long
  ) = MeetingAction(parse.json) { request =>
    request.body.validate[AgendaItemForm] match {
      case e: JsError => {
        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[AgendaItemForm] => {
        val form = FullAgendaItemForm(request.meeting, s.get)
        AgendaItemsDao.validate(form) match {
          case Nil => {
            val org = AgendaItemsDao.create(request.user, form)
            Created(Json.toJson(org))
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

}
