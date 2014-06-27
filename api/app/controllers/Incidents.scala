package controllers

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ IncidentsDao, Incident, IncidentForm, User }

object Incidents extends Controller {

  private lazy val user = User(guid = UUID.randomUUID) // TODO

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
        Conflict(Json.toJson(Seq(Error("100", "invalid json: " + e.toString))))
      }
      case s: JsSuccess[IncidentForm] => {
        val form = s.get
        form.validate match {
          case None => {
            val incident = IncidentsDao.create(user, s.get)
            Created(Json.toJson(incident)).withHeaders(LOCATION -> routes.Incidents.getById(incident.id).url)
          }
          case Some(error) => {
            Conflict(Json.toJson(Seq(Error("101", "Validation error: " + error))))
          }
        }
      }
    }
  }

  def putById(id: Long) = Action(parse.json) { request =>
    IncidentsDao.findById(id) match {
      case None => NotFound
      case Some(i: Incident) => {
        request.body.validate[IncidentForm] match {
          case e: JsError => {
            Conflict(Json.toJson(Error("100", "invalid json")))
          }
          case s: JsSuccess[IncidentForm] => {
            val updated = IncidentsDao.update(user, i, s.get)
            Created(Json.toJson(updated)).withHeaders(LOCATION -> routes.Incidents.getById(updated.id).url)
          }
        }
      }
    }
  }

  def deleteById(id: Long) = Action { request =>
    IncidentsDao.findById(id).foreach { i =>
      IncidentsDao.softDelete(user, i)
    }
    NoContent
  }

}
