package controllers

import client.Api
import com.gilt.quality.models.Team
import lib.{ Pagination, PaginatedCollection }
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object TeamMembers extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def show(
    org: String,
    key: String,
    page: Int = 0
  ) = TeamAction.async { implicit request =>
    for {
      members <- Api.instance.teams.getMembersByOrgAndKey(
        org = org,
        key = key,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
      isMemberCollection <- Api.instance.teams.getMembersByOrgAndKey(
        org = org,
        key = key,
        userGuid = Some(request.user.guid),
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      val isMember = !isMemberCollection.isEmpty
      Ok(views.html.team_members.show(request.mainTemplate(), request.team, PaginatedCollection(page, members), isMember))
    }
  }

  def add(
    org: String,
    key: String
  ) = TODO

  def postLeave(
    org: String,
    key: String
  ) = TODO

  def postJoin(
    org: String,
    key: String
  ) = TODO

}
