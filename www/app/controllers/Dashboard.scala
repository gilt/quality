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
      teamStatisics <- Api.instance.TeamStatistics.get(seconds=Some(564800))
      events <- Api.instance.Events.get(numberHours = Some(24), limit = Some(10))
    } yield {
      Ok(views.html.dashboard.index(
        teamStatisics.entity,
        events.entity
      ))
    }
  }

}
