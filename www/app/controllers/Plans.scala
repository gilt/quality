package controllers

import client.Api
import quality.models.{ Error, Plan }

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object Plans extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def postDeleteById(id: Long, incidentId: Long) = Action.async { implicit request =>
    for {
      planResult <- Api.instance.Plans.deleteById(id)
    } yield {
      Redirect(routes.Incidents.show(incidentId)).flashing("success" -> s"Plan deleted")
    }
  }

  def uploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    for {
      incidentResult <- Api.instance.Incidents.get(id = Some(incidentId))
      planResult <- Api.instance.Plans.get(incidentId = Some(incidentId))
    } yield {
      incidentResult.entity.headOption match {
        case None => Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident) => {
          val form = planForm.fill(
            PlanForm(body = planResult.entity.headOption.map(_.body).getOrElse(""))
          )
          Ok(views.html.plans.upload(incident, form))
        }
      }
    }
  }

  def postUploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    for {
      incidentResult <- Api.instance.Incidents.get(id = Some(incidentId))
      planResult <- Api.instance.Plans.get(incidentId = Some(incidentId))
    } yield {
      val incident = incidentResult.entity.headOption.getOrElse {
        sys.error("Invalid incident")
      }

      val boundForm = planForm.bindFromRequest
      boundForm.fold (

        formWithErrors => {
          Ok(views.html.plans.upload(incident, formWithErrors))
        },

        planForm => {
          planResult.entity.headOption match {

            case None => {
              Await.result(
                Api.instance.Plans.post(
                  incidentId = incident.id,
                  body = planForm.body
                ).map { r =>
                  Redirect(routes.Incidents.show(r.entity.id)).flashing("success" -> "Plan created")
                }.recover {
                  case quality.FailedResponse(errors: Seq[quality.models.Error], 409) => {
                    Ok(views.html.plans.upload(incident, boundForm, Some(errors.map(_.message).mkString("\n"))))
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
                ).map { r =>
                  Redirect(routes.Incidents.show(r.entity.id)).flashing("success" -> "Plan updated")
                }.recover {
                  case quality.FailedResponse(errors: Seq[quality.models.Error], 409) => {
                    Ok(views.html.plans.upload(incident, boundForm, Some(errors.map(_.message).mkString("\n"))))
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

  case class PlanForm(
    body: String
  )

  private val planForm = Form(
    mapping(
      "body" -> text
    )(PlanForm.apply)(PlanForm.unapply)
  )

}
