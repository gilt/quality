package controllers

import client.Api
import com.gilt.quality.models.{ Error, Incident, Plan }

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object Plans extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getById(id: Long) = Action.async { implicit request =>
    Api.instance.plans.getById(id).map {
      case None => {
        Redirect(routes.Incidents.index()).flashing("warning" -> "Plan not found")
      }
      case Some(plan: Plan) => {
        Redirect(routes.Incidents.show(plan.incidentId))
      }
    }
  }

  def postDeleteById(id: Long, incidentId: Long) = Action.async { implicit request =>
    for {
      planResult <- Api.instance.plans.deleteById(id)
    } yield {
      Redirect(routes.Incidents.show(incidentId)).flashing("success" -> s"Plan deleted")
    }
  }

  def uploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    for {
      incidentOption <- Api.instance.incidents.getById(incidentId)
      plansResult <- Api.instance.plans.get(incidentId = Some(incidentId))
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
      incidentOption <- Api.instance.incidents.getById(incidentId)
      plans <- Api.instance.plans.get(incidentId = Some(incidentId))
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
                    Api.instance.plans.post(
                      incidentId = incident.id,
                      body = planForm.body
                    ).map { plan =>
                      Redirect(routes.Incidents.show(plan.incidentId)).flashing("success" -> "Plan created")
                    }.recover {
                      case response: com.gilt.quality.error.ErrorsResponse => {
                        Ok(views.html.plans.upload(incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
                      }
                    }
                    , 1000.millis
                  )
                }

                case Some(plan: Plan) => {
                  Await.result(
                    Api.instance.plans.putById(
                      id = plan.id,
                      incidentId = incident.id,
                      body = planForm.body
                    ).map { plan =>
                      Redirect(routes.Incidents.show(plan.incidentId)).flashing("success" -> "Plan updated")
                    }.recover {
                      case response: com.gilt.quality.error.ErrorsResponse => {
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
    Api.instance.plans.getById(id).flatMap {
      case None => Future {
        Redirect(routes.Incidents.index()).flashing("warning" -> "Plan not found")
      }
      case Some(plan: Plan) => {
        Api.instance.plans.putGradeById(plan.id, grade).map { plan =>
          Redirect(routes.Incidents.show(plan.incidentId)).flashing("success" -> "Plan updated")
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
