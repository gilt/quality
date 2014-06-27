package controllers

import quality.models.Healthcheck
import quality.models.json._
import play.api._
import play.api.mvc._
import play.api.libs.json._

object Healthchecks extends Controller {

  private val Healthy = Healthcheck("health")

  def get() = Action { request =>
    Ok(Json.toJson(Healthy))
  }

}
