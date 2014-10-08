package controllers

import db.EventsDao
import com.gilt.quality.models.{Event, Model}
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object Events extends Controller {

  def get(model: Option[Model], action: Option[com.gilt.quality.models.Action], number_hours: Option[Int], limit: Int = 25, offset: Int = 0) = Action { request =>
    val events = EventsDao.findAll(
      model = model,
      action = action,
      numberHours = number_hours,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(events.toSeq))
  }

}
