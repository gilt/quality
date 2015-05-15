package controllers

import client.Api
import lib.{ Pagination, PaginatedCollection }

import play.api._
import play.api.mvc._
import play.api.Play.current

object Dashboard extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def index(
    org: String,
    agendaItemsPage: Int = 0
  ) = OrgAction.async { implicit request =>
    for {
      myStats <- Api.instance.Statistics.getByOrg(org, userGuid = Some(request.user.guid))
      agendaItems <- Api.instance.agendaItems.getByOrg(
        org = org,
        userGuid = Some(request.user.guid),
        isAdjourned = Some(false),
        limit = Pagination.DefaultLimit+1,
        offset = agendaItemsPage * Pagination.DefaultLimit
      )
      meetings <- Api.instance.meetings.getByOrg(
        org = org,
        isAdjourned = Some(false),
        isUpcoming = Some(true),
        orderBy = Some("meetings.scheduled_at:asc"),
        limit = 1
      )
    } yield {
      Ok(
        views.html.dashboard.index(
          request.mainTemplate(),
          request.org,
          myStats,
          meetings.headOption,
          PaginatedCollection(agendaItemsPage, agendaItems)
        )
      )
    }
  }

}
