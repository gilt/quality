package controllers

import quality.models.{ Error, Plan }
import quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{ GradeForm, GradesDao, PlansDao, PlanForm, User }

object Plans extends Controller {

  def get(id: Option[Long], incident_id: Option[Long], team_key: Option[String], limit: Int = 25, offset: Int = 0) = Action { Request =>
    val matches = PlansDao.findAll(
      id = id,
      incidentId = incident_id,
      teamKey = team_key,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getById(id: Long) = Action {
    PlansDao.findById(id) match {
      case None => NotFound
      case Some(i: Plan) => Ok(Json.toJson(i))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[PlanForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Seq(Error("100", "invalid json: " + e.toString))))
      }
      case s: JsSuccess[PlanForm] => {
        val form = s.get
        form.validate match {
          case Nil => {
            val plan = PlansDao.create(User.Default, form)
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
      case Some(i: Plan) => {
        request.body.validate[PlanForm] match {
          case e: JsError => {
            Conflict(Json.toJson(Error("100", "invalid json")))
          }
          case s: JsSuccess[PlanForm] => {
            val form = s.get
            form.validate match {
              case Nil => {
                val updated = PlansDao.update(User.Default, i, s.get)
                Ok(Json.toJson(updated)).withHeaders(LOCATION -> routes.Plans.getById(updated.id).url)
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

  def putGradeById(id: Long) = Action(parse.json) { request =>
    (request.body \ "grade").asOpt[Int] match {
      case None => Conflict(Json.toJson(Seq(Error("100", "missing grade in json body"))))
      case Some(grade: Int) => {
        if (grade < 0 || grade > 100) {
          Conflict(Json.toJson(Seq(Error("101", s"grade[$grade] must be >= 0 and <= 100"))))
        } else {
          PlansDao.findById(id) match {
            case None => NotFound
            case Some(plan: Plan) => {
              val form = GradeForm(plan_id = plan.id, score = grade)
              GradesDao.upsert(User.Default, form)
              val updated = PlansDao.findById(id).get
              Ok(Json.toJson(updated)).withHeaders(LOCATION -> routes.Plans.getById(updated.id).url)
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
