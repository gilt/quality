package controllers

import client.Api
import com.gilt.quality.v0.models.{AdjournForm, Meeting, Task}
import com.gilt.quality.v0.errors.UnitResponse
import lib.{Pagination, PaginatedCollection}

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
        limit = Pagination.DefaultLimit+1,
        offset = page * Pagination.DefaultLimit
      )
    } yield {
     Ok(views.html.meetings.index(request.mainTemplate(), request.org, PaginatedCollection(page, meetings)))
    }
  }

  def show(
    org: String,
    id: Long,
    reviewTeamsPage: Int = 0,
    reviewPlansPage: Int = 0
  ) = OrgAction.async { implicit request =>
    Api.instance.meetings.getByOrgAndId(org, id).flatMap { meeting =>
      for {
        reviewTeams <- Api.instance.agendaItems.getByOrg(
          org = org,
          meetingId = Some(id),
          task = Some(Task.ReviewTeam),
          limit = Pagination.DefaultLimit+1,
          offset = reviewTeamsPage * Pagination.DefaultLimit
        )
        reviewPlans <- Api.instance.agendaItems.getByOrg(
          org = org,
          meetingId = Some(id),
          task = Some(Task.ReviewPlan),
          limit = Pagination.DefaultLimit+1,
          offset = reviewPlansPage * Pagination.DefaultLimit
        )
      } yield {
        Ok(
          views.html.meetings.show(
            request.mainTemplate(),
            request.org,
            meeting,
            PaginatedCollection(reviewTeamsPage, reviewTeams),
            PaginatedCollection(reviewPlansPage, reviewPlans)
          )
        )
      }
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Meetings.index(org)).flashing("warning" -> s"Meeting $id not found")
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

  def postAdjournById(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    for {
      result <- Api.instance.meetings.postAdjournByOrgAndId(
        org = org,
        id = id,
        adjournForm = com.gilt.quality.v0.models.AdjournForm()
      )
    } yield {
      Redirect(routes.Meetings.show(org, id)).flashing("success" -> s"Meeting $id adjourned")
    }
  }

}
