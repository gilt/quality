package controllers

import db.{IncidentOrganizationChangesDao, IncidentsDao, UsersDao}
import lib.Validation
import com.gilt.quality.models.{IncidentOrganizationChange, User}
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object IncidentOrganizationChanges extends Controller with IncidentOrganizationChanges

trait IncidentOrganizationChanges {
  this: Controller =>

  def post() = Action(parse.json) { request =>
    println("POST A")
    request.body.validate[IncidentOrganizationChange] match {
      case e: JsError => {
    println("POST B")

        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[IncidentOrganizationChange] => {
        println("POST C")
        val form = s.get
        println("POST D")
        IncidentOrganizationChangesDao.validate(form) match {
          case Nil => {
            IncidentOrganizationChangesDao.process(UsersDao.Default, form)
            IncidentsDao.findById(form.incidentId) match {
              case None => NotFound
              case Some(i) => Ok(Json.toJson(i))
            }
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

}
