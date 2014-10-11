package controllers

import com.gilt.quality.models.{Error, Incident, IncidentForm, Plan}
import com.gilt.quality.models.json._
import lib.Validation
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullIncidentForm, IncidentsDao, OrganizationsDao, User}

object Incidents extends Controller {

  def getByOrg(
    org: String,
    id: Option[Long],
    team_key: Option[String],
    has_team: Option[Boolean],
    has_plan: Option[Boolean],
    has_grade: Option[Boolean],
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val matches = IncidentsDao.findAll(
      orgKey = Some(request.org.key),
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

  def getByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    IncidentsDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(i: Incident) => Ok(Json.toJson(i))
    }
  }

  def postByOrg(org: String) = OrgAction(parse.json) { request =>
    request.body.validate[IncidentForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[IncidentForm] => {
        val fullForm = FullIncidentForm(request.org, s.get)
        fullForm.validate match {
          case Nil => {
            val incident = IncidentsDao.create(request.user, fullForm)
            Created(Json.toJson(incident)).withHeaders(LOCATION -> routes.Incidents.getByOrgAndId(request.org.key, incident.id).url)
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def putByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction(parse.json) { request =>
    IncidentsDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(i: Incident) => {
        request.body.validate[IncidentForm] match {
          case e: JsError => {
            Conflict(Json.toJson(Validation.invalidJson(e)))
          }
          case s: JsSuccess[IncidentForm] => {
            val fullForm = FullIncidentForm(request.org, s.get)
            fullForm.validate match {
              case Nil => {
                val updated = IncidentsDao.update(request.user, i, fullForm)
                Ok(Json.toJson(updated)).withHeaders(LOCATION -> routes.Incidents.getByOrgAndId(request.org.key, updated.id).url)
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

  def deleteByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    IncidentsDao.findByOrganizationAndId(request.org, id).foreach { i =>
      IncidentsDao.softDelete(request.user, i)
    }
    NoContent
  }

}
