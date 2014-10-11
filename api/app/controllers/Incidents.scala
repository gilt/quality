package controllers

import com.gilt.quality.models.{Error, Incident, IncidentForm, Plan}
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullIncidentForm, IncidentsDao, OrganizationsDao, User}

object Incidents extends Controller {

  val orgKey = "gilt" // TODO
  lazy val org = OrganizationsDao.findByKey(orgKey).get // TODO

  def get(
    id: Option[Long],
    team_key: Option[String],
    has_team: Option[Boolean],
    has_plan: Option[Boolean],
    has_grade: Option[Boolean],
    limit: Int = 25,
    offset: Int = 0
  ) = Action { request =>
    val matches = IncidentsDao.findAll(
      orgKey = Some(org.key),
      id = id,
      teamKey = team_key,
      hasTeam = has_team,
      hasPlan = has_plan,
      hasGrade = has_grade,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getById(id: Long) = Action {
    IncidentsDao.findByOrganizationAndId(org, id) match {
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
        val fullForm = FullIncidentForm(org, s.get)
        fullForm.validate match {
          case Nil => {
            val incident = IncidentsDao.create(User.Default, fullForm)
            Created(Json.toJson(incident)).withHeaders(LOCATION -> routes.Incidents.getById(incident.id).url)
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def putById(id: Long) = Action(parse.json) { request =>
    IncidentsDao.findByOrganizationAndId(org, id) match {
      case None => NotFound
      case Some(i: Incident) => {
        request.body.validate[IncidentForm] match {
          case e: JsError => {
            Conflict(Json.toJson(Error("100", "invalid json")))
          }
          case s: JsSuccess[IncidentForm] => {
            val fullForm = FullIncidentForm(org, s.get)
            fullForm.validate match {
              case Nil => {
                val updated = IncidentsDao.update(User.Default, i, fullForm)
                Created(Json.toJson(updated)).withHeaders(LOCATION -> routes.Incidents.getById(updated.id).url)
              }
              case errors => {
                Conflict(Json.toJson(errors))
              }
            }
          }
        }
      }
    }
  }

  def deleteById(id: Long) = Action { request =>
    IncidentsDao.findByOrganizationAndId(org, id).foreach { i =>
      IncidentsDao.softDelete(User.Default, i)
    }
    NoContent
  }

}
