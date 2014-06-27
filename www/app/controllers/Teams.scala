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

  def index(key: Option[String] = None, page: Int = 0) = Action.async { implicit request =>
    for {
      teams <- Api.instance.Teams.get(
        key = key,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.teams.index(key, PaginatedCollection(page, teams.entity)))
    }
  }

  def show(key: String) = Action.async { implicit request =>
    Api.instance.Teams.getByKey(key).map { r =>
      Ok(views.html.teams.show(r.entity))
    }.recover {
      case quality.FailedResponse(_, 404) => {
        Redirect(routes.Teams.index()).flashing("warning" -> s"Team $key not found")
      }
    }
  }

  def create() = Action { implicit request =>
    Ok(views.html.teams.create(teamForm))
  }

  def postCreate() = Action { implicit request =>
    val boundForm = teamForm.bindFromRequest
    boundForm.fold (

      formWithErrors => {
        Ok(views.html.teams.create(formWithErrors))
      },

      teamForm => {
        Await.result(Api.instance.Teams.get(
          key = Some(teamForm.key),
          limit = Some(1)
        ), 1000.millis).entity.headOption match {
          case Some(t) => {
            Ok(views.html.teams.create(boundForm, Some("Team with this key already exists")))
          }
          case None => {
            val team = Await.result(Api.instance.Teams.post(
              key = teamForm.key
            ), 1000.millis).entity

            Redirect(routes.Teams.show(team.key)).flashing("success" -> "Team created")
          }
        }
      }

    )
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
