package controllers

import quality.models.{ Error, Report }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ ReportsDao, ReportValidator, ReportWithId, User }

object Reports extends Controller {

  def get(id: Option[Long], incident_id: Option[Long], limit: Int = 25, offset: Int = 0) = Action { Request =>
    val matches = ReportsDao.findAll(
      id = id,
      incidentId = incident_id,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getById(id: Long) = Action {
    ReportsDao.findById(id) match {
      case None => NotFound
      case Some(i: ReportWithId) => Ok(Json.toJson(i))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[Report] match {
      case e: JsError => {
        Conflict(Json.toJson(Seq(Error("100", "invalid json: " + e.toString))))
      }
      case s: JsSuccess[Report] => {
        val form = s.get
        // TODO: Check if report already exists - need to validate
        val report = ReportsDao.create(User.Default, s.get)
        Created(Json.toJson(report)).withHeaders(LOCATION -> routes.Reports.getById(report.id).url)
      }
    }
  }

  def putById(id: Long) = Action(parse.json) { request =>
    ReportsDao.findById(id) match {
      case None => NotFound
      case Some(i: ReportWithId) => {
        request.body.validate[Report] match {
          case e: JsError => {
            Conflict(Json.toJson(Error("100", "invalid json")))
          }
          case s: JsSuccess[Report] => {
            val form = s.get
            ReportValidator.validate(form) match {
              case Nil => {
                val updated = ReportsDao.update(User.Default, i, s.get)
                Created(Json.toJson(updated)).withHeaders(LOCATION -> routes.Reports.getById(updated.id).url)
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
    ReportsDao.findById(id).foreach { i =>
      ReportsDao.softDelete(User.Default, i)
    }
    NoContent
  }

  def putGradeById(id: Long) = Action { request =>
    ReportsDao.findById(id) match {
      case None => NotFound
      case (r: ReportWithId) => {
        ReportsDao.update((User.Default, r, grade)
      }
    }
    NoContent

  }

}
