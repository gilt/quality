package controllers

import com.gilt.quality.v0.models.json._
import play.api.mvc._
import play.api.libs.json._
import db.StatisticsDao
import java.util.UUID

object Statistics extends Controller {

  def getByOrg(
    org: String,
    userGuid: Option[UUID],
    teamKey: Option[String]
  ) = OrgAction { request =>
    val matches = StatisticsDao.findAll(
      org = request.org,
      teamKey = teamKey,
      userGuid = userGuid
    )

    Ok(Json.toJson(matches.toSeq))
  }
}
