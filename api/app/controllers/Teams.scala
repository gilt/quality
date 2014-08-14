package controllers

import com.gilt.quality.models.{ Error, Team }
import com.gilt.quality.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ TeamsDao, TeamForm, User }

object Teams extends Controller {

  private lazy val user = User(guid = UUID.randomUUID) // TODO

  def get(key: Option[String], limit: Int = 25, offset: Int = 0) = Action { request =>
    val matches = TeamsDao.findAll(
      key = key,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getByKey(key: String) = Action {
    TeamsDao.findByKey(key) match {
      case None => NotFound
      case Some(t: Team) => Ok(Json.toJson(t))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[TeamForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Seq(Error("100", "invalid json: " + e.toString))))
      }
      case s: JsSuccess[TeamForm] => {
        val form = s.get
        TeamsDao.findByKey(form.key) match {
          case None => {
            val team = TeamsDao.create(user, form)
            Created(Json.toJson(team)).withHeaders(LOCATION -> routes.Teams.getByKey(team.key).url)
          }
          case Some(t: Team) => {
            Conflict(Json.toJson(Seq(Error("team_key_exists", "A team with this key already exists"))))
          }
        }
      }
    }
  }

  def deleteByKey(key: String) = Action { request =>
    TeamsDao.findByKey(key).foreach { i =>
      TeamsDao.softDelete(user, i)
    }
    NoContent
  }

}
