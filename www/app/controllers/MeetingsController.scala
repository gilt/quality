package controllers

import client.Api
import com.gilt.quality.models.Meeting
import lib.{ Pagination, PaginatedCollection }
import java.util.UUID
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object Meetings extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def index(
    org: String,
    page: Int = 0
  ) = OrgAction.async { implicit request =>
    for {
      meetings <- Api.instance.meetings.getByOrg(
        org = org,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.meetings.index(request.org, PaginatedCollection(page, meetings)))
    }
  }

  def show(
    org: String,
    id: Long,
    agendaItemsPage: Int = 0
  ) = OrgAction.async { implicit request =>
    for {
      meeting <- Api.instance.meetings.getByOrgAndId(org, id)
      agendaItems <- Api.instance.agendaItems.getMeetingsAndAgendaItemsByOrgAndMeetingId(
        org = org,
        meetingId = id,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(agendaItemsPage * Pagination.DefaultLimit)
      )
    } yield {
      meeting match {
        case None => {
          Redirect(routes.Meetings.index(org)).flashing("warning" -> s"Meeting $id not found")
        }
        case Some(meeting: Meeting) => {
          Ok(views.html.meetings.show(request.org, meeting, PaginatedCollection(agendaItemsPage, agendaItems)))
        }
      }
    }
  }

  def postDeleteById(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    for {
      result <- Api.instance.meetings.deleteByOrgAndId(org, id)
    } yield {
      Redirect(routes.Meetings.index(org)).flashing("success" -> s"Meeting $id deleted")
    }
  }

}
