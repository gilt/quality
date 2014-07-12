package controllers

import client.Api
import quality.models.Team
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

  def index(key: Option[String] = None, page: Int = 0) = Action.async { implicit request =>
    val filters = Filters(key = lib.Filters.toOption(key))
    for {
      teams <- Api.instance.teams.get(
        key = filters.key,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.teams.index(filters, PaginatedCollection(page, teams)))
    }
  }

  def show(key: String, incidentsPage: Int = 0) = Action.async { implicit request =>
    for {
      team <- Api.instance.teams.getByKey(key)
      stats <- Api.instance.Statistics.get(teamKey = Some(key), numberHours = Some(Dashboard.OneWeekInHours * 12))
      incidents <- Api.instance.incidents.get(
        teamKey = Some(key),
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(incidentsPage * Pagination.DefaultLimit)
      )
    } yield {
      team match {
        case None => {
          Redirect(routes.Teams.index()).flashing("warning" -> s"Team $key not found")
        }
        case Some(team: Team) => {
          Ok(views.html.teams.show(team, stats.headOption, PaginatedCollection(incidentsPage, incidents)))
        }
      }
    }
  }

  def create() = Action { implicit request =>
    Ok(views.html.teams.create(teamForm))
  }

  def postCreate() = Action.async { implicit request =>
    val boundForm = teamForm.bindFromRequest
    boundForm.fold (

      formWithErrors => Future {
        Ok(views.html.teams.create(formWithErrors))
      },

      teamForm => {
        Api.instance.teams.post(teamForm.key).map { team =>
          Redirect(routes.Teams.show(team.key)).flashing("success" -> "Team created")
        }.recover {
          case response: quality.error.ErrorsResponse => {
            Ok(views.html.teams.create(boundForm, Some(response.errors.map(_.message).mkString(", "))))
          }
        }
      }

    )
  }

  def postDeleteByKey(key: String) = Action.async { implicit request =>
    for {
      result <- Api.instance.teams.deleteByKey(key)
    } yield {
      Redirect(routes.Teams.index()).flashing("success" -> s"Team $key deleted")
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
