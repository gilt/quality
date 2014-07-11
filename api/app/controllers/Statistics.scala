package controllers

import quality.models.{ Error, Statistic }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ StatisticsDao, StatisticForm, User }

object Statistics extends Controller {

  def get(team_key: Option[String], number_hours: Int = 168) = Action { Request =>
    val matches = StatisticsDao.findAll(
      teamKey = team_key,
      numberHours = number_hours
    )

    Ok(Json.toJson(matches.toSeq))
  }
}
