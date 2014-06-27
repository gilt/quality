package controllers

import client.Api
import lib.{ Pagination, PaginatedCollection }

import play.api._
import play.api.mvc._
import play.api.Play.current

object Dashboard extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def index(page: Int = 0) = Action.async { implicit request =>
    for {
      teams <- Api.instance.Teams.get(
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.dashboard.index(PaginatedCollection(page, teams.entity)))
    }
  }

}
