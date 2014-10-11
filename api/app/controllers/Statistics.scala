package controllers

import com.gilt.quality.models.{Error, Statistic}
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{StatisticsDao, User}

object Statistics extends Controller {

  def getByOrg(
    org: String,
    team_key: Option[String],
    number_hours: Int = 168
  ) = OrgAction { request =>
    val matches = StatisticsDao.findAll(
      orgKey = request.org.key,
      teamKey = team_key,
      numberHours = number_hours
    )

    Ok(Json.toJson(matches.toSeq))
  }
}
