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

  def get(
    meetingId: Long,
    id: Option[Long],
    task: Option[com.gilt.quality.models.Task],
    limit: Int = 25,
    offset: Int = 0
  ) = Action {
    val items = AgendaItemsDao.findAll(
      meetingId = Some(meetingId),
      id = id,
      task = task,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(items))
  }

  def getById(meetingId: Long, id: Long) = Action {
    AgendaItemsDao.findByMeetingIdAndId(meetingId, id) match {
      case None => NotFound
      case Some(item) => Ok(Json.toJson(item))
    }
  }

  def deleteById(meetingId: Long, id: Long) = Action {
    AgendaItemsDao.findByMeetingIdAndId(meetingId, id).map { item =>
      AgendaItemsDao.softDelete(User.Default, item)
    }
    NoContent
  }

  def post(meetingId: Long) = Action(parse.json) { request =>
    MeetingsDao.findById(meetingId) match {
      case None => NotFound
      case Some(meeting) => {
        request.body.validate[AgendaItemForm] match {
          case e: JsError => {
            BadRequest(Json.toJson(Validation.invalidJson(e)))
          }
          case s: JsSuccess[AgendaItemForm] => {
            val form = FullAgendaItemForm(meeting, s.get)
            AgendaItemsDao.validate(form) match {
              case Nil => {
                val org = AgendaItemsDao.create(User.Default, form)
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
  }

}
