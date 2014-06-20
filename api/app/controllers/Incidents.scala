package controllers

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.IncidentsDao

object Incidents extends Controller {

  val incidents = scala.collection.mutable.ListBuffer[Incident]()

  case class Error(code: String, message: String)

  object Error {
    implicit val errorWrites = Json.writes[Error]
  }

  case class Incident(guid: String, summary: String, description: String, team_key: String, severity: String)

  object Incident {
    implicit val incidentWrites = Json.writes[Incident]
  }

  case class IncidentForm(summary: String, description: String, team_key: String, severity: String)

  object IncidentForm {
    implicit val incidentFormReads = Json.reads[IncidentForm]
  }

  def get(guid: Option[String], team_key: Option[String], limit: Int = 25, offset: Int = 0) = Action { request =>
    val matches = incidents.filter { p => guid.isEmpty || Some(p.guid) == guid }
    Ok(Json.toJson(matches.toSeq))
  }

  def getByGuid(guid: String) = Action {
    incidents.find { _.guid == guid } match {
      case None => NotFound
      case Some(i: Incident) => Ok(Json.toJson(i))
    }
  }

  def post() = Action(parse.json) { request =>
    println("BODY: " + parse.json)

    request.body.validate[IncidentForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Error("100", "invalid json")))
      }
      case s: JsSuccess[IncidentForm] => {
        val form = s.get

        val incident = Incident(
          guid = UUID.randomUUID.toString,
          summary = form.summary,
          description = form.description,
          team_key = form.team_key,
          severity = form.severity
        )
        incidents += incident
        Created(Json.toJson(incident)).withHeaders(LOCATION -> routes.Incidents.getByGuid(incident.guid).url)
      }
    }
  }

  def deleteByGuid(guid: String) = Action { request =>
    incidents.find { _.guid == guid }.foreach { p =>
      incidents -= p
    }
    NoContent
  }

}
