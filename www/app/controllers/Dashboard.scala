package controllers

import client.Api

import play.api._
import play.api.mvc._
import play.api.Play.current

object Dashboard extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  val OneDayInHours = 24
  val OneWeekInHours = OneDayInHours * 7

  def index(org: String) = Action.async { implicit request =>
    for {
      stats <- Api.instance.Statistics.getByOrg(org, numberHours = Some(OneWeekInHours))
      events <- Api.instance.Events.getByOrg(org, numberHours = Some(OneDayInHours), limit = Some(10))
    } yield {
      Ok(views.html.dashboard.index(stats, events))
    }
  }

}
