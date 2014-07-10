package controllers

import quality.models.{ Error, TeamStatistic }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ TeamStatisticsDao, TeamStatisticForm, User }

object TeamStatistics extends Controller {

  def get(team_key: Option[String], seconds: Option[Long]) = Action { Request =>
    val matches = TeamStatisticsDao.findAll(
        key = team_key,
        seconds = seconds
      )

    Ok(Json.toJson(matches.toSeq))
  }
}
