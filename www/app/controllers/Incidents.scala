package controllers

import client.Api
import quality.models.Incident
import lib.{ Pagination, PaginatedCollection }
import java.util.UUID
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object Incidents extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def index(teamKey: Option[String], page: Int = 0) = Action.async { implicit request =>
    for {
      incidents <- Api.instance.Incidents.get(
        teamKey = teamKey,
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      Ok(views.html.incidents.index(teamKey, PaginatedCollection(page, incidents.entity)))
    }
  }

  def show(id: Long) = Action.async { implicit request =>
    Api.instance.Incidents.getById(id).map { r =>
      Ok(views.html.incidents.show(r.entity))
    }.recover {
      case quality.FailedResponse(_, 404) => {
        Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $id not found")
      }
    }
  }

  def create() = Action { implicit request =>
    Ok(views.html.incidents.create(incidentForm))
  }

  def postCreate() = Action.async { implicit request =>
    val boundForm = incidentForm.bindFromRequest
    boundForm.fold (

      formWithErrors => Future {
        Ok(views.html.incidents.create(formWithErrors))
      },

      incidentForm => {
        Api.instance.Incidents.post(
          summary = incidentForm.summary,
          description = incidentForm.description,
          teamKey = incidentForm.teamKey,
          severity = incidentForm.severity,
          tags = incidentForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty)
        ).map { r =>
          Redirect(routes.Incidents.show(r.entity.id)).flashing("success" -> "Incident created")
        }.recover {
          case quality.FailedResponse(errors: Seq[quality.models.Error], 409) => {
            Ok(views.html.incidents.create(boundForm, Some(errors.map(_.message).mkString("\n"))))
          }
        }
      }
    )
  }

  def edit(id: Long) = Action.async { implicit request =>
    Api.instance.Incidents.getById(id).map { r =>
      val incident = r.entity
      val form = incidentForm.fill(
        IncidentForm(
          summary = incident.summary,
          description = incident.description,
          teamKey = incident.teamKey,
          severity = incident.severity.toString,
          tags = incident.tags.mkString(" ")
        )
      )
      Ok(views.html.incidents.edit(incident, form))
    }.recover {
      case quality.FailedResponse(_, 404) => {
        Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $id not found")
      }
    }
  }

  def postEdit(id: Long) = Action.async { implicit request =>
    Api.instance.Incidents.getById(id).map { r =>
      val incident = r.entity

      incidentForm.bindFromRequest.fold (

        formWithErrors => {
          Ok(views.html.incidents.edit(incident, formWithErrors))
        },

        incidentForm => {
          Await.result(Api.instance.Incidents.putById(
            id = incident.id,
            summary = incidentForm.summary,
            description = incidentForm.description,
            teamKey = incidentForm.teamKey,
            severity = incidentForm.severity,
            tags = incidentForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty)
          ), 1000.millis).entity

          Redirect(routes.Incidents.show(incident.id)).flashing("success" -> "Incident updated")
        }

      )

    }.recover {
      case quality.FailedResponse(_, 404) => {
        Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $id not found")
      }
    }
  }

  case class IncidentForm(
    summary: String,
    description: Option[String],
    teamKey: String,
    severity: String,
    tags: String
  )

  private val incidentForm = Form(
    mapping(
      "summary" -> nonEmptyText,
      "description" -> optional(text),
      "teamKey" -> nonEmptyText,
      "severity" -> nonEmptyText,
      "tags" -> text
    )(IncidentForm.apply)(IncidentForm.unapply)
  )

}
