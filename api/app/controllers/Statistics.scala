package controllers

import quality.models.{ Error, Statistic }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ StatisticsDao, StatisticForm, User }

object Statistics extends Controller {

  def get(team_key: Option[String], seconds: Option[Long]) = Action { Request =>
    val matches = StatisticsDao.findAll(
        key = team_key,
        seconds = seconds
      )

    Ok(Json.toJson(matches.toSeq))
  }
}
