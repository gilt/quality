package controllers

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ IncidentsDao, Incident, IncidentForm, User }

object Incidents extends Controller {

  case class Error(code: String, message: String)

  object Error {
    implicit val errorWrites = Json.writes[Error]
  }
  def get(id: Option[Long], team_key: Option[String], limit: Int = 25, offset: Int = 0) = Action { request =>
    val matches = IncidentsDao.findAll(
      id = id,
      teamKey = team_key,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getById(id: Long) = Action {
    IncidentsDao.findById(id) match {
      case None => NotFound
      case Some(i: Incident) => Ok(Json.toJson(i))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[IncidentForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Error("100", "invalid json")))
      }
      case s: JsSuccess[IncidentForm] => {
        val user = User(guid = UUID.randomUUID) // TODO
        val incident = IncidentsDao.create(user, s.get)
        Created(Json.toJson(incident)).withHeaders(LOCATION -> routes.Incidents.getById(incident.id).url)
      }
    }
  }

  def deleteById(id: Long) = Action { request =>
    val user = User(guid = UUID.randomUUID) // TODO
    IncidentsDao.findById(id).foreach { i =>
      IncidentsDao.softDelete(user, i)
    }
    NoContent
  }

}
