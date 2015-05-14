package controllers

import client.Api
import com.gilt.quality.v0.errors.UnitResponse
import com.gilt.quality.v0.models.{Error, Incident, Organization, Plan, PlanForm}
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
    Api.instance.plans.getByOrgAndId(org, id).map { plan =>
      Redirect(routes.Incidents.show(org, plan.incidentId))
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> "Plan not found")
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
    Api.instance.incidents.getByOrgAndId(org, incidentId).flatMap { incident =>

      for {
        plansResult <- Api.instance.plans.getByOrg(org, incidentId = Some(incidentId))
      } yield {
        val plan = plansResult.headOption
        val form = uiForm.fill(
          UiForm(body = plan.map(_.body).getOrElse(""))
        )
          Ok(views.html.plans.upload(request.mainTemplate(), request.org, incident, form))
      }

    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $incidentId not found")
      }
    }
  }

  def postUploadByIncidentId(
    org: String,
    incidentId: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, incidentId).flatMap { incident =>

      for {
        plans <- Api.instance.plans.getByOrg(org, incidentId = Some(incidentId))
      } yield {
        val boundForm = uiForm.bindFromRequest
        boundForm.fold (

          formWithErrors => {
            Ok(views.html.plans.upload(request.mainTemplate(), request.org, incident, formWithErrors))
          },

          uiForm => {
            plans.headOption match {

              case None => {
                Await.result(
                  createPlan(request.org, incident, uiForm.body).map { plan =>
                    Redirect(routes.Incidents.show(org, plan.incidentId)).flashing("success" -> "Plan created")
                  }.recover {
                    case response: com.gilt.quality.v0.errors.ErrorsResponse => {
                      Ok(views.html.plans.upload(request.mainTemplate(), request.org, incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
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
                    case response: com.gilt.quality.v0.errors.ErrorsResponse => {
                      Ok(views.html.plans.upload(request.mainTemplate(), request.org, incident, boundForm, Some(response.errors.map(_.message).mkString("\n"))))
                    }
                  }
                    , 1000.millis
                )
              }
            }
          }
        )
      }
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $incidentId not found")
      }
    }
  }

  def postNoPlan(
    org: String,
    incidentId: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, incidentId).flatMap { incident =>

      for {
        plansResult <- Api.instance.plans.getByOrg(org, incidentId = Some(incidentId))
      } yield {
        plansResult.headOption match {
          case None => {
            Await.result(
              createPlan(request.org, incident, PlanNotAvailable).flatMap { plan =>
                Api.instance.plans.putGradeByOrgAndId(org, plan.id, GradeImage.Bad)
              },
              1000.millis
            )
              Redirect(routes.Incidents.show(org, incident.id)).flashing("success" -> s"Default plan created and marked as Bad")
          }
          case Some(plan) => {
            Redirect(routes.Incidents.show(org, incident.id)).flashing("warning" -> s"Incident $incidentId has a plan")
          }
        }
      }
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $incidentId not found")
      }
    }
  }

  def postGrade(
    org: String,
    id: Long,
    grade: Int
  ) = OrgAction.async { implicit request =>
    Api.instance.plans.getByOrgAndId(org, id).flatMap { plan =>
      Api.instance.plans.putGradeByOrgAndId(org, plan.id, grade).map { plan =>
        Redirect(routes.Incidents.show(org, plan.incidentId)).flashing("success" -> "Plan updated")
      }
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> "Plan not found")
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
