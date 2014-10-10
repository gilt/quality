package controllers

import db.{AgendaItemsDao, User}
import lib.Validation
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object AgendaItems extends Controller with AgendaItems

trait AgendaItems {
  this: Controller =>

  def get(
    meetingId: Long,
    task: Option[com.gilt.quality.models.Task],
    limit: Int = 25,
    offset: Int = 0
  ) = Action {
    val items = AgendaItemsDao.findAll(
      meetingId = Some(meetingId),
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

  def post(meetingId: Long) = TODO

}
