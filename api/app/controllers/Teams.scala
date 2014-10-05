package controllers

import com.gilt.quality.models.{Team, TeamForm}
import com.gilt.quality.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullTeamForm, OrganizationsDao, TeamsDao, User}
import lib.Validation

object Teams extends Controller {

  val orgKey = "gilt" // TODO
  lazy val org = OrganizationsDao.findByKey(orgKey).get // TODO

  def get(key: Option[String], limit: Int = 25, offset: Int = 0) = Action { request =>
    val matches = TeamsDao.findAll(
      orgKey = orgKey,
      key = key,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getByKey(key: String) = Action {
    TeamsDao.findByKey(org, key) match {
      case None => NotFound
      case Some(t: Team) => Ok(Json.toJson(t))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[TeamForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[TeamForm] => {
        val fullForm = FullTeamForm(org, s.get)
        fullForm.validate match {
          case Nil => {
            val team = TeamsDao.create(User.Default, fullForm)
            Created(Json.toJson(team)).withHeaders(LOCATION -> routes.Teams.getByKey(team.key).url)
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def deleteByKey(key: String) = Action { request =>
    TeamsDao.findByKey(org, key).foreach { i =>
      TeamsDao.softDelete(User.Default, i)
    }
    NoContent
  }

}
