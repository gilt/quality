package controllers

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ ReportsDao, Report, ReportForm, User }

object Reports extends Controller {

  def get(id: Option[Long], incident_id: Option[Long], limit: Int = 25, offset: Int = 0) = Action { Request =>
    val matches = ReportsDao.findAll(
      id = id,
      incidentId = incident_id,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

}
