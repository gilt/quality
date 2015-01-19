package controllers

import client.Api
import lib.{ Pagination, PaginatedCollection }

import play.api._
import play.api.mvc._
import play.api.Play.current

object Dashboard extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  val OneDayInHours = 24
  val OneWeekInHours = OneDayInHours * 7

  def index(
    org: String,
    agendaItemsPage: Int = 0
  ) = OrgAction.async { implicit request =>
    for {
      stats <- Api.instance.Statistics.getByOrg(org, numberHours = Some(OneWeekInHours))
      agendaItems <- Api.instance.agendaItems.getByOrg(
        org = org,
        userGuid = Some(request.user.guid),
        isAdjourned = Some(false),
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(agendaItemsPage * Pagination.DefaultLimit)
      )
      meetings <- Api.instance.meetings.getByOrg(
        org = org,
        isAdjourned = Some(false),
        isUpcoming = Some(true),
        orderBy = Some("meetings.scheduled_at:asc"),
        limit = Some(1)
      )
    } yield {
      Ok(
        views.html.dashboard.index(
          request.mainTemplate(),
          request.org,
          stats,
          meetings.headOption,
          PaginatedCollection(agendaItemsPage, agendaItems)
        )
      )
    }
  }

}
