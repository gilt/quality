package controllers

import db.EventsDao
import com.gilt.quality.models.Event
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object Events extends Controller {

  def get(model: Option[String], action: Option[String], number_hours: Option[Int], limit: Int = 25, offset: Int = 0) = Action { request =>
    val events = EventsDao.findAll(
      model = model,
      numberHours = number_hours,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(events.toSeq))
  }

}
