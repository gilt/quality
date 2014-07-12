package controllers

import client.Api
import lib.{ Pagination, PaginatedCollection }

import play.api._
import play.api.mvc._
import play.api.Play.current

object Dashboard extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val OneDayInHours = 24
  private val OneWeekInHours = OneDayInHours * 7

  def index(page: Int = 0) = Action.async { implicit request =>
    for {
      statisics <- Api.instance.Statistics.get(numberHours = Some(OneWeekInHours))
      events <- Api.instance.Events.get(numberHours = Some(OneDayInHours), limit = Some(10))
    } yield {
      Ok(views.html.dashboard.index(
        statisics,
        events
      ))
    }
  }

}
