package controllers

import client.Api
import com.gilt.quality.v0.errors.UnitResponse
import com.gilt.quality.v0.models.Team
import lib.{ Pagination, PaginatedCollection }
import java.util.UUID
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object Teams extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Filters(key: Option[String])

  def index(
    org: String,
    key: Option[String] = None,
    myPage: Int = 0,
    otherPage: Int = 0
  ) = OrgAction.async { implicit request =>
    val filters = Filters(key = lib.Filters.toOption(key))
    for {
      myTeams <- Api.instance.teams.getByOrg(
        org = org,
        key = filters.key,
        userGuid = Some(request.user.guid),
        limit = Pagination.DefaultLimit+1,
        offset = myPage * Pagination.DefaultLimit
      )
      otherTeams <- Api.instance.teams.getByOrg(
        org = org,
        key = filters.key,
        excludeUserGuid = Some(request.user.guid),
        limit = Pagination.DefaultLimit+1,
        offset = otherPage * Pagination.DefaultLimit
      )
    } yield {
      Ok(views.html.teams.index(request.mainTemplate(), request.org, filters, PaginatedCollection(myPage, myTeams), PaginatedCollection(otherPage, otherTeams)))
    }
  }

  def show(
    org: String,
    key: String,
    agendaItemsPage: Int = 0,
    membersPage: Int = 0
  ) = TeamAction.async { implicit request =>
    for {
      stats <- Api.instance.Statistics.getByOrg(org = org, teamKey = Some(key), numberHours = Dashboard.OneWeekInHours * 12)
      agendaItems <- Api.instance.agendaItems.getByOrg(
        org = org,
        teamKey = Some(key),
        isAdjourned = Some(false),
        limit = Pagination.DefaultLimit+1,
        offset = agendaItemsPage * Pagination.DefaultLimit
      )
      members <- Api.instance.teams.getMembersByOrgAndKey(
        org = org,
        key = key,
        limit = Pagination.DefaultLimit+1,
        offset = membersPage * Pagination.DefaultLimit
      )
      isMemberCollection <- Api.instance.teams.getMembersByOrgAndKey(
        org = org,
        key = key,
        userGuid = Some(request.user.guid)
      )
      memberSummary <- Api.instance.teams.getMemberSummaryByOrgAndKey(
        org = org,
        key = key
      ).map { s => Some(s) }.recover { case UnitResponse(404) => None }
    } yield {
      Ok(
        views.html.teams.show(
          request.mainTemplate(),
          request.team,
          stats.headOption,
          memberSummary.head,
          PaginatedCollection(agendaItemsPage, agendaItems),
          PaginatedCollection(membersPage, members),
          isMember = !isMemberCollection.isEmpty
        )
      )
    }
  }

  def create(org: String) = OrgAction { implicit request =>
    Ok(views.html.teams.create(request.mainTemplate(), request.org, teamForm))
  }

  def postCreate(org: String) = OrgAction.async { implicit request =>
    val boundForm = teamForm.bindFromRequest
    boundForm.fold (

      formWithErrors => Future {
        Ok(views.html.teams.create(request.mainTemplate(), request.org, formWithErrors))
      },

      teamForm => {
        val form = com.gilt.quality.v0.models.TeamForm(
          key = teamForm.key,
          email = teamForm.email,
          smileyUrl = teamForm.smileyUrl,
          frownyUrl = teamForm.frownyUrl
        )
        Api.instance.teams.postByOrg(org = org, teamForm = form).map { team =>
          Redirect(routes.Teams.show(org, team.key)).flashing("success" -> "Team created")
        }.recover {
          case response: com.gilt.quality.v0.errors.ErrorsResponse => {
            Ok(views.html.teams.create(request.mainTemplate(), request.org, boundForm, Some(response.errors.map(_.message).mkString(", "))))
          }
        }
      }

    )
  }

  def edit(org: String, key: String) = OrgAction.async { implicit request =>
    Api.instance.teams.getByOrgAndKey(org, key).map { team =>
      val form = teamForm.fill(
        TeamForm(
          key = team.key,
          email = team.email,
          smileyUrl = Some(team.icons.smileyUrl),
          frownyUrl = Some(team.icons.frownyUrl)
        )
      )

      Ok(views.html.teams.edit(request.mainTemplate(), team, form))
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Teams.index(org)).flashing("success" -> s"Team not found")
      }
    }
  }

  def postEdit(org: String, key: String) = TeamAction.async { implicit request =>
    val boundForm = teamForm.bindFromRequest
    boundForm.fold (
      formWithErrors => {
        Future {
          Ok(views.html.teams.edit(request.mainTemplate(), request.team, formWithErrors))
        }
      },

      teamForm => {
        val form = com.gilt.quality.v0.models.UpdateTeamForm(
          email = teamForm.email,
          smileyUrl = teamForm.smileyUrl,
          frownyUrl = teamForm.frownyUrl
        )
        Api.instance.teams.putByOrgAndKey(org = org, key = request.team.key, updateTeamForm = form).map { team =>
          Redirect(routes.Teams.show(org, team.key)).flashing("success" -> "Team created")
        }.recover {
          case response: com.gilt.quality.v0.errors.ErrorsResponse => {
            Ok(views.html.teams.create(request.mainTemplate(), request.org, boundForm, Some(response.errors.map(_.message).mkString(", "))))
          }
        }
      }
    )
  }

  def postDeleteByKey(
    org: String,
    key: String
  ) = OrgAction.async { implicit request =>
    for {
      result <- Api.instance.teams.deleteByOrgAndKey(org, key)
    } yield {
      Redirect(routes.Teams.index(org)).flashing("success" -> s"Team $key deleted")
    }
  }

  case class TeamForm(
    key: String,
    email: Option[String],
    smileyUrl: Option[String],
    frownyUrl: Option[String]
  )

  private val teamForm = Form(
    mapping(
      "key" -> nonEmptyText,
      "email" -> optional(text),
      "smiley_url" -> optional(text),
      "frowny_url" -> optional(text)
    )(TeamForm.apply)(TeamForm.unapply)
  )

}
