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

  case class Filters(teamKey: Option[String], hasTeam: Option[String], hasPlan: Option[String], hasGrade: Option[String])

  // Max number of teams we'll show in a drop down before switching to text input for team key
  private val MaxTeams = 1000

  def index(teamKey: Option[String], hasTeam: Option[String], hasPlan: Option[String], hasGrade: Option[String], page: Int = 0) = Action.async { implicit request =>
    val filters = Filters(
      teamKey = lib.Filters.toOption(teamKey),
      hasTeam = lib.Filters.toOption(hasTeam),
      hasPlan = lib.Filters.toOption(hasPlan),
      hasGrade = lib.Filters.toOption(hasGrade)
    )

    for {
      incidents <- Api.instance.Incidents.get(
        teamKey = filters.teamKey,
        hasTeam = filters.hasTeam.map(_.toInt > 0),
        hasPlan = filters.hasPlan.map(_.toInt > 0),
        hasGrade = filters.hasGrade.map(_.toInt > 0),
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
      teams <- Api.instance.Teams.get(limit = Some(MaxTeams))
    } yield {
      val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
      Ok(views.html.incidents.index(filters, PaginatedCollection(page, incidents), teamsOrEmpty))
    }
  }

  def show(id: Long) = Action.async { implicit request =>
    for {
      incidents <- Api.instance.Incidents.get(id = Some(id))
      plans <- Api.instance.Plans.get(incidentId = Some(id))
    } yield {
      incidents.headOption match {
        case None => Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $id not found")
        case Some(incident) => {
          Ok(views.html.incidents.show(incident, plans.headOption))
        }
      }
    }
  }

  def postDeleteById(id: Long) = Action.async { implicit request =>
    for {
      result <- Api.instance.Incidents.deleteById(id)
    } yield {
      Redirect(routes.Incidents.index()).flashing("success" -> s"Incident $id deleted")
    }
  }

  def create(teamKey: Option[String] = None) = Action { implicit request =>
    val form = incidentForm.fill(
      IncidentForm(
        summary = "",
        description = Some(util.ExampleIncident.description),
        teamKey = teamKey,
        severity = "",
        tags = ""
      )
    )
    Ok(views.html.incidents.create(form))
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
        ).map { incident =>
            Redirect(routes.Incidents.show(incident.id)).flashing("success" -> "Incident created")
        }.recover {
          case response: quality.ErrorResponse => {
            Ok(views.html.incidents.create(boundForm, Some(response.errors.map(_.message).mkString("\n"))))
          }
        }
      }
    )
  }

  def edit(id: Long) = Action.async { implicit request =>
    for {
      response <- Api.instance.Incidents.getById(id)
    } yield {
      response match {
        case None => {
          Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $id not found")
        }
        case Some(incident: Incident) => {
          val form = incidentForm.fill(
            IncidentForm(
              summary = incident.summary,
              description = incident.description,
              teamKey = incident.team.map(_.key),
              severity = incident.severity.toString,
              tags = incident.tags.mkString(" ")
            )
          )
          Ok(views.html.incidents.edit(incident, form))
        }
      }
    }
  }

  def postEdit(id: Long) = Action.async { implicit request =>
    for {
      response <- Api.instance.Incidents.getById(id)
    } yield {
      response match {
        case None => {
          Redirect(routes.Incidents.index()).flashing("warning" -> s"Incident $id not found")
        }

        case Some(incident: Incident) => {
          val boundForm = incidentForm.bindFromRequest
          boundForm.fold (

            formWithErrors => {
              Ok(views.html.incidents.edit(incident, formWithErrors))
            },

            incidentForm => {
              Await.result(
                Api.instance.Incidents.putById(
                  id = incident.id,
                  summary = incidentForm.summary,
                  description = incidentForm.description,
                  teamKey = incidentForm.teamKey,
                  severity = incidentForm.severity,
                  tags = incidentForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty)
                ).map { r =>
                  Redirect(routes.Incidents.show(incident.id)).flashing("success" -> "Incident updated")
                }.recover {
                  case response: quality.ErrorResponse => {
                    Ok(views.html.incidents.create(boundForm, Some(response.errors.map(_.message).mkString("\n"))))
                  }
                },
                1000.millis
              )
            }
          )
        }
      }
    }
  }

  case class IncidentForm(
    summary: String,
    description: Option[String],
    teamKey: Option[String],
    severity: String,
    tags: String
  )

  private val incidentForm = Form(
    mapping(
      "summary" -> nonEmptyText,
      "description" -> optional(text),
      "teamKey" -> optional(text),
      "severity" -> nonEmptyText,
      "tags" -> text
    )(IncidentForm.apply)(IncidentForm.unapply)
  )

}
