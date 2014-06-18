package controllers

import client.Api
import lib.{ Pagination, PaginatedCollection }

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index(incidentsPage: Int = 0, membershipRequestsPage: Int = 0) = Action.async { implicit request =>
    for {
      incidents <- Api.instance.Incidents.get(
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(incidentsPage * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.index(PaginatedCollection(incidentsPage, incidents.entity)))
    }
  }

}
