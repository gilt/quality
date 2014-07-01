package controllers

import quality.models.{ Error, Plan }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ PlansDao, PlanValidator, PlanWithId, User }

object Plans extends Controller {

  def get(id: Option[Long], incident_id: Option[Long], limit: Int = 25, offset: Int = 0) = Action { Request =>
    val matches = PlansDao.findAll(
      id = id,
      incidentId = incident_id,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getById(id: Long) = Action {
    PlansDao.findById(id) match {
      case None => NotFound
      case Some(i: PlanWithId) => Ok(Json.toJson(i))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[Plan] match {
      case e: JsError => {
        Conflict(Json.toJson(Seq(Error("100", "invalid json: " + e.toString))))
      }
      case s: JsSuccess[Plan] => {
        val form = s.get
        PlanValidator.validate(form) match {
          case Nil => {
            val plan = PlansDao.create(User.Default, s.get)
            Created(Json.toJson(plan)).withHeaders(LOCATION -> routes.Plans.getById(plan.id).url)
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def putById(id: Long) = Action(parse.json) { request =>
    PlansDao.findById(id) match {
      case None => NotFound
      case Some(i: PlanWithId) => {
        request.body.validate[Plan] match {
          case e: JsError => {
            Conflict(Json.toJson(Error("100", "invalid json")))
          }
          case s: JsSuccess[Plan] => {
            val form = s.get
            PlanValidator.validate(form) match {
              case Nil => {
                val updated = PlansDao.update(User.Default, i, s.get)
                Created(Json.toJson(updated)).withHeaders(LOCATION -> routes.Plans.getById(updated.id).url)
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
    PlansDao.findById(id).foreach { i =>
      PlansDao.softDelete(User.Default, i)
    }
    NoContent
  }

}
