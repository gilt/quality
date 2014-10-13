package controllers

import com.gilt.quality.models.{Team, TeamForm}
import com.gilt.quality.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullTeamForm, OrganizationsDao, TeamsDao, User}
import lib.Validation

object Teams extends Controller {

  def getByOrg(
    org: String,
    key: Option[String],
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val matches = TeamsDao.findAll(
      request.org,
      key = key,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getByOrgAndKey(
    org: String,
    key: String
  ) = OrgAction { request =>
    TeamsDao.findByKey(request.org, key) match {
      case None => NotFound
      case Some(t: Team) => Ok(Json.toJson(t))
    }
  }

  def postByOrg(org: String) = OrgAction(parse.json) { request =>
    request.body.validate[TeamForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[TeamForm] => {
        val fullForm = FullTeamForm(request.org, s.get)
        fullForm.validate match {
          case Nil => {
            val team = TeamsDao.create(request.user, fullForm)
            Created(Json.toJson(team)).withHeaders(LOCATION -> routes.Teams.getByOrgAndKey(request.org.key, team.key).url)
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def deleteByOrgAndKey(
    org: String,
    key: String
  ) = OrgAction { request =>
    TeamsDao.findByKey(request.org, key).foreach { i =>
      TeamsDao.softDelete(request.user, i)
    }
    NoContent
  }

}
