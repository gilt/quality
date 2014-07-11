package controllers

import quality.models.{ Error, Statistic }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ StatisticsDao, StatisticForm, User }

object Statistics extends Controller {

  def get(team_key: Option[String], num_days: Option[Int]) = Action { Request =>
    val matches = StatisticsDao.findAll(
        key = team_key,
        numDays = num_days
      )

    Ok(Json.toJson(matches.toSeq))
  }
}
