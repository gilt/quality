package controllers

import client.Api
import com.gilt.quality.models.Team
import lib.{ Pagination, PaginatedCollection }
import java.util.UUID
import scala.concurrent.{ Await, Future }
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
    page: Int = 0
  ) = Action.async { implicit request =>
    val filters = Filters(key = lib.Filters.toOption(key))
    for {
      teams <- Api.instance.teams.getByOrg(
        org = org,
        key = filters.key,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.teams.index(filters, PaginatedCollection(page, teams)))
    }
  }

  def show(
    org: String,
    key: String,
    incidentsPage: Int = 0
  ) = Action.async { implicit request =>
    for {
      team <- Api.instance.teams.getByOrgAndKey(org, key)
      stats <- Api.instance.Statistics.getByOrg(org = org, teamKey = Some(key), numberHours = Some(Dashboard.OneWeekInHours * 12))
      incidents <- Api.instance.incidents.getByOrg(
        org = org,
        teamKey = Some(key),
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(incidentsPage * Pagination.DefaultLimit)
      )
    } yield {
      team match {
        case None => {
          Redirect(routes.Teams.index(org)).flashing("warning" -> s"Team $key not found")
        }
        case Some(team: Team) => {
          Ok(views.html.teams.show(team, stats.headOption, PaginatedCollection(incidentsPage, incidents)))
        }
      }
    }
  }

  def create(org: String) = Action { implicit request =>
    Ok(views.html.teams.create(teamForm))
  }

  def postCreate(org: String) = Action.async { implicit request =>
    val boundForm = teamForm.bindFromRequest
    boundForm.fold (

      formWithErrors => Future {
        Ok(views.html.teams.create(formWithErrors))
      },

      teamForm => {
        val form = com.gilt.quality.models.TeamForm(
          key = teamForm.key
        )
        Api.instance.teams.postByOrg(org = org, teamForm = form).map { team =>
          Redirect(routes.Teams.show(org, team.key)).flashing("success" -> "Team created")
        }.recover {
          case response: com.gilt.quality.error.ErrorsResponse => {
            Ok(views.html.teams.create(boundForm, Some(response.errors.map(_.message).mkString(", "))))
          }
        }
      }

    )
  }

  def postDeleteByKey(
    org: String,
    key: String
  ) = Action.async { implicit request =>
    for {
      result <- Api.instance.teams.deleteByOrgAndKey(org, key)
    } yield {
      Redirect(routes.Teams.index(org)).flashing("success" -> s"Team $key deleted")
    }
  }

  case class TeamForm(
    key: String
  )

  private val teamForm = Form(
    mapping(
      "key" -> nonEmptyText
    )(TeamForm.apply)(TeamForm.unapply)
  )

}
