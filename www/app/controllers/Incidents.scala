package controllers

import client.Api
import quality.models.Incident
import lib.{ Pagination, PaginatedCollection }
import java.util.UUID

import play.api._
import play.api.mvc._

object Incidents extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def show(guid: UUID) = Action.async { implicit request =>
    for {
      incidentResponse <- Api.instance.Incidents.get(guid = Some(guid))
    } yield {
      incidentResponse.entity.headOption match {
        case None => NotFound
        case Some(i: Incident) => {
          Ok(views.html.incidents.show(i))
        }
      }
    }
  }

  def create() = TODO

}
