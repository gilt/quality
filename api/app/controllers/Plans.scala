package controllers

import com.gilt.quality.models.{Error, Organization, Plan, PlanForm}
import com.gilt.quality.models.json._
import lib.Validation
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullPlanForm, GradeForm, GradesDao, IncidentsDao, PlansDao, User}

object Plans extends Controller {

  def getByOrg(
    org: String,
    id: Option[Long],
    incident_id: Option[Long],
    team_key: Option[String],
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val matches = PlansDao.findAll(
      orgKey = request.org.key,
      id = id,
      incidentId = incident_id,
      teamKey = team_key,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(matches.toSeq))
  }

  def getByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    PlansDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(i: Plan) => Ok(Json.toJson(i))
    }
  }

  def postByOrg(org: String) = OrgAction(parse.json) { request =>
    request.body.validate[PlanForm] match {
      case e: JsError => {
        Conflict(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[PlanForm] => {
        IncidentsDao.findByOrganizationAndId(request.org, s.get.incidentId) match {
          case None => {
            NotFound
          }
          case Some(incident) => {
            val form = FullPlanForm(request.org, incident, s.get)
            form.validate match {
              case Nil => {
                val plan = PlansDao.create(request.user, form)
                Created(Json.toJson(plan)).withHeaders(LOCATION -> routes.Plans.getByOrgAndId(request.org.key, plan.id).url)
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

  def putByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction(parse.json) { request =>
    PlansDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(plan: Plan) => {
        request.body.validate[PlanForm] match {
          case e: JsError => {
            Conflict(Json.toJson(Validation.invalidJson(e)))
          }
          case s: JsSuccess[PlanForm] => {
            println("org: " + org)
            println("plan: " + plan)
            IncidentsDao.findByOrganizationAndId(request.org, plan.incidentId) match {
              case None => NotFound
              case Some(incident) => {
                println("form: " +s.get)
                val form = FullPlanForm(request.org, incident, s.get)
                form.validate match {
                  case Nil => {
                    val updated = PlansDao.update(request.user, plan, form)
                    Ok(Json.toJson(updated)).withHeaders(LOCATION -> routes.Plans.getByOrgAndId(request.org.key, updated.id).url)
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
    }
  }

  def putGradeByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction(parse.json) { request =>
    (request.body \ "grade").asOpt[Int] match {
      case None => {
        Conflict(Json.toJson(Validation.error("missing grade in json body")))
      }
      case Some(grade: Int) => {
        if (grade < 0 || grade > 100) {
          Conflict(Json.toJson(Validation.error(s"grade[$grade] must be >= 0 and <= 100")))
        } else {
          PlansDao.findByOrganizationAndId(request.org, id) match {
            case None => NotFound
            case Some(plan: Plan) => {
              val form = GradeForm(plan_id = plan.id, score = grade)
              GradesDao.upsert(request.user, form)
              val updated = PlansDao.findByOrganizationAndId(request.org, id).get
              Ok(Json.toJson(updated)).withHeaders(LOCATION -> routes.Plans.getByOrgAndId(request.org.key, updated.id).url)
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
    PlansDao.findByOrganizationAndId(request.org, id).foreach { plan =>
      PlansDao.softDelete(request.user, plan)
    }
    NoContent
  }

}
