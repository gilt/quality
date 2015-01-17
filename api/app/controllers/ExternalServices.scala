package controllers

import com.gilt.quality.v0.models.{ExternalService, ExternalServiceForm, ExternalServiceName, User}
import com.gilt.quality.v0.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullExternalServiceForm, ExternalServicesDao}
import lib.Validation

object ExternalServices extends Controller {

  def getByOrg(
    org: String,
    id: Option[Long],
    name: Option[ExternalServiceName],
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val matches = ExternalServicesDao.findAll(
      request.org,
      id = id,
      name = name,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches))
  }

  def getByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    ExternalServicesDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(s: ExternalService) => Ok(Json.toJson(s))
    }
  }

  def postByOrg(org: String) = OrgAction(parse.json) { request =>
    request.body.validate[ExternalServiceForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[ExternalServiceForm] => {
        val fullForm = FullExternalServiceForm(request.org, s.get)
        fullForm.validate match {
          case Nil => {
            val service = ExternalServicesDao.create(request.user, fullForm)
            Created(Json.toJson(service)).withHeaders(LOCATION -> routes.ExternalServices.getByOrgAndId(request.org.key, service.id).url)
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def deleteByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    ExternalServicesDao.findByOrganizationAndId(request.org, id).foreach { i =>
      ExternalServicesDao.softDelete(request.user, i)
    }
    NoContent
  }

}
