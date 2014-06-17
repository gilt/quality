package controllers

import lib.{ Pagination, PaginatedCollection }
import java.util.UUID

import play.api._
import play.api.mvc._

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val apiUrl = current.configuration.getString("quality.url").getOrElse {
    sys.error("quality.url is required")
  }

  private val apiToken = current.configuration.getString("quality.token").getOrElse {
    sys.error("quality.token is required")
  }

  lazy val api = new quality.Client(apiUrl, Some(apiToken))

  def index(incidentsPage: Int = 0, membershipRequestsPage: Int = 0) = Action.async { implicit request =>
    for {
      incidents <- api.Incidents.get(
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(incidentsPage * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.index(PaginatedCollection(incidentsPage, incidents.entity)))
    }
  }

}
