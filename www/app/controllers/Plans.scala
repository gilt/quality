package controllers

import client.Api
import quality.models.{ Error, Incident, Plan }

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object Plans extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getById(id: Long) = Action.async { implicit request =>
    for {
      plan <- Api.instance.Plans.getById(id)
    } yield {
      plan match {
        case None => {
          Redirect(routes.Incidents.index()).flashing("warning" -> "Plan not found")
        }
        case Some(plan: Plan) => {
          Redirect(routes.Incidents.show(plan.incidentId))
        }
      }
    }
  }

  def postDeleteById(id: Long, incidentId: Long) = Action.async { implicit request =>
    for {
      planResult <- Api.instance.Plans.deleteById(id)
    } yield {
      Redirect(routes.Incidents.show(incidentId)).flashing("success" -> s"Plan deleted")
    }
  }

  def uploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    for {
      incidentOption <- Api.instance.Incidents.getById(incidentId)
      plansResult <- Api.instance.Plans.get(incidentId = Some(incidentId))
    } yield {
      incidentOption match {
        case None => Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident: Incident) => {
          val plan = plansResult.headOption
          val form = planForm.fill(
            PlanForm(body = plan.map(_.body).getOrElse(""))
          )
          Ok(views.html.plans.upload(incident, form))
        }
      }
    }
  }

  def postUploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    for {
      incidentOption <- Api.instance.Incidents.getById(incidentId)
      plans <- Api.instance.Plans.get(incidentId = Some(incidentId))
    } yield {
      incidentOption match {
        case None => Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident: Incident) => {
          val boundForm = planForm.bindFromRequest
          boundForm.fold (

            formWithErrors => {
              Ok(views.html.plans.upload(incident, formWithErrors))
            },

            planForm => {
              plans.headOption match {

                case None => {
                  Await.result(
                    Api.instance.Plans.post(
                      incidentId = incident.id,
                      body = planForm.body
                    ).map { plan =>
                      Redirect(routes.Incidents.show(plan.incidentId)).flashing("success" -> "Plan created")
                    }.recover {
                      case response: quality.ErrorResponse => {
                        Ok(views.html.plans.upload(incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
                      }
                    }
                    , 1000.millis
                  )
                }

                case Some(plan: Plan) => {
                  Await.result(
                    Api.instance.Plans.putById(
                      id = plan.id,
                      incidentId = incident.id,
                      body = planForm.body
                    ).map { plan =>
                      Redirect(routes.Incidents.show(plan.incidentId)).flashing("success" -> "Plan updated")
                    }.recover {
                      case response: quality.ErrorResponse => {
                        Ok(views.html.plans.upload(incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
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

  def postGrade(id: Long, grade: Int) = Action.async { implicit request =>
    for {
      planOption <- Api.instance.Plans.getById(id)
    } yield {
      planOption match {
        case None => {
          Redirect(routes.Incidents.index()).flashing("warning" -> "Plan not found")
        }
        case Some(plan: Plan) => {
          Await.result(
            Api.instance.Plans.putGradeById(plan.id, grade).map { plan =>
              Redirect(routes.Incidents.show(plan.incidentId)).flashing("success" -> "Plan updated")
            }
              , 1000.millis
          )
        }
      }
    }
  }

  case class PlanForm(
    body: String
  )

  private val planForm = Form(
    mapping(
      "body" -> text
    )(PlanForm.apply)(PlanForm.unapply)
  )

}
