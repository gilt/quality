package controllers

import db.{OrganizationsDao, UsersDao}
import lib.Validation
import com.gilt.quality.v0.models.{OrganizationForm, User}
import com.gilt.quality.v0.models.json._
import play.api.mvc._
import play.api.libs.json._

object Organizations extends Controller with Organizations

trait Organizations {
  this: Controller =>

  def get(
    id: Option[Long] = None,
    key: Option[String] = None,
    limit: Int = 25,
    offset: Int = 0
  ) = Action {
    val orgs = OrganizationsDao.findAll(
      id = id,
      key = key,
      limit = limit,
      offset = offset
    )
    Ok(Json.toJson(orgs))
  }

  def getByKey(key: String) = Action {
    OrganizationsDao.findByKey(key) match {
      case None => NotFound
      case Some(org) => Ok(Json.toJson(org))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[OrganizationForm] match {
      case e: JsError => {
        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[OrganizationForm] => {
        val form = s.get
        OrganizationsDao.validate(form) match {
          case Nil => {
            val org = OrganizationsDao.create(UsersDao.Default, form)
            Created(Json.toJson(org))
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def deleteByKey(key: String) = Action { request =>
    OrganizationsDao.findByKey(key).map { org =>
      OrganizationsDao.softDelete(UsersDao.Default, org)
    }
    NoContent
  }


}
