package controllers

import db.OrganizationsDao
import com.gilt.quality.v0.models.Healthcheck
import com.gilt.quality.v0.models.json._
import play.api._
import play.api.mvc._
import play.api.libs.json._

object Healthchecks extends Controller {

  private val Healthy = Json.toJson(Healthcheck("healthy"))

  /**
   *  Verifies we can communicate with the database
   */
  def getInternalAndHealthcheck() = Action { request =>
    OrganizationsDao.findAll(limit = 1)
    Ok(Healthy)
  }

}
