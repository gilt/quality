package controllers

import db.EventsDao
import quality.models.Event
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object Events extends Controller {

  def get(model: Option[String], action: Option[String], limit: Int = 25, offset: Int = 0) = Action { request =>
    val events = EventsDao.findAll(
      model = model,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(events.toSeq))
  }

}
