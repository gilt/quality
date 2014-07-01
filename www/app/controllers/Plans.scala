package controllers

import client.Api
import quality.models.Plan

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object Plans extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def uploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    for {
      incidentResult <- Api.instance.Incidents.get(id = Some(incidentId))
      planResult <- Api.instance.Plans.get(incidentId = Some(incidentId))
    } yield {
      incidentResult.entity.headOption match {
        case None => Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $incidentId not found")
        case Some(incident) => {
          Ok(views.html.plans.upload(incident, planResult.entity.headOption))
        }
      }
    }
  }

  def postUploadByIncidentId(incidentId: Long) = Action.async { implicit request =>
    sys.error("TODO")
  }

}
