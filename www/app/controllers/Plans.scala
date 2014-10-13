package controllers

import client.Api
import com.gilt.quality.models.{Error, Incident, Organization, Plan, PlanForm}
import lib.GradeImage

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object Plans extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  val PlanNotAvailable = "No plan available at time of review"

  def getById(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.plans.getByOrgAndId(org, id).map {
      case None => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> "Plan not found")
      }
      case Some(plan: Plan) => {
        Redirect(routes.Incidents.show(org, plan.incidentId))
      }
    }
  }

  def postDeleteById(
    org: String,
    id: Long,
    incidentId: Long
  ) = OrgAction.async { implicit request =>
    for {
      planResult <- Api.instance.plans.deleteByOrgAndId(org, id)
    } yield {
      Redirect(routes.Incidents.show(org, incidentId)).flashing("success" -> s"Plan deleted")
    }
  }

  def uploadByIncidentId(
    org: String,
    incidentId: Long
  ) = OrgAction.async { implicit request =>
    for {
      incidentOption <- Api.instance.incidents.getByOrgAndId(org, incidentId)
      plansResult <- Api.instance.plans.getByOrg(org, incidentId = Some(incidentId))
    } yield {
      incidentOption match {
        case None => Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident: Incident) => {
          val plan = plansResult.headOption
          val form = uiForm.fill(
            UiForm(body = plan.map(_.body).getOrElse(""))
          )
          Ok(views.html.plans.upload(request.org, incident, form))
        }
      }
    }
  }

  def postUploadByIncidentId(
    org: String,
    incidentId: Long
  ) = OrgAction.async { implicit request =>
    for {
      incidentOption <- Api.instance.incidents.getByOrgAndId(org, incidentId)
      plans <- Api.instance.plans.getByOrg(org, incidentId = Some(incidentId))
    } yield {
      incidentOption match {
        case None => Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident: Incident) => {
          val boundForm = uiForm.bindFromRequest
          boundForm.fold (

            formWithErrors => {
              Ok(views.html.plans.upload(request.org, incident, formWithErrors))
            },

            uiForm => {
              plans.headOption match {

                case None => {
                  Await.result(
                    createPlan(request.org, incident, uiForm.body).map { plan =>
                      Redirect(routes.Incidents.show(org, plan.incidentId)).flashing("success" -> "Plan created")
                    }.recover {
                      case response: com.gilt.quality.error.ErrorsResponse => {
                        Ok(views.html.plans.upload(request.org, incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
                      }
                    }
                    , 1000.millis
                  )
                }

                case Some(plan: Plan) => {
                  Await.result(
                    Api.instance.plans.putByOrgAndId(
                      org = org,
                      id = plan.id,
                      planForm = PlanForm(
                        incidentId = incident.id,
                        body = uiForm.body
                      )
                    ).map { plan =>
                      Redirect(routes.Incidents.show(org, plan.incidentId)).flashing("success" -> "Plan updated")
                    }.recover {
                      case response: com.gilt.quality.error.ErrorsResponse => {
                        Ok(views.html.plans.upload(request.org, incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
                      }
                    }
                    , 1000.millis
                  )
                }

              }
            }
          )
        }
      }
    }
  }

  def postNoPlan(
    org: String,
    incidentId: Long
  ) = OrgAction.async { implicit request =>
    for {
      incidentOption <- Api.instance.incidents.getByOrgAndId(org, incidentId)
      plansResult <- Api.instance.plans.getByOrg(org, incidentId = Some(incidentId))
    } yield {
      incidentOption match {
        case None => Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident: Incident) => {
          plansResult.headOption match {
            case None => {
              Await.result(
                createPlan(request.org, incident, PlanNotAvailable).flatMap { plan =>
                  Api.instance.plans.putGradeByOrgAndId(org, plan.id, GradeImage.Bad)
                },
                1000.millis
              )
              Redirect(routes.Incidents.show(org, incident.id)).flashing("warning" -> s"Incident $incidentId has a plan")
            }
            case Some(plan) => {
              Redirect(routes.Incidents.show(org, incident.id)).flashing("warning" -> s"Incident $incidentId has a plan")
            }
          }
        }
      }
    }
  }

  def postGrade(
    org: String,
    id: Long,
    grade: Int
  ) = OrgAction.async { implicit request =>
    Api.instance.plans.getByOrgAndId(org, id).flatMap {
      case None => Future {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> "Plan not found")
      }
      case Some(plan: Plan) => {
        Api.instance.plans.putGradeByOrgAndId(org, plan.id, grade).map { plan =>
          Redirect(routes.Incidents.show(org, plan.incidentId)).flashing("success" -> "Plan updated")
        }
      }
    }
  }

  case class UiForm(
    body: String
  )

  private val uiForm = Form(
    mapping(
      "body" -> text
    )(UiForm.apply)(UiForm.unapply)
  )

  private def createPlan(
    org: Organization,
    incident: Incident,
    body: String
  ): Future[Plan] = {
    Api.instance.plans.postByOrg(
      org = org.key,
      planForm = PlanForm(
        incidentId = incident.id,
        body = body
      )
    )
  }

}
