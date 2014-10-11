package controllers

import client.Api
import com.gilt.quality.models.{Incident, IncidentForm, Severity, Team}
import lib.{Pagination, PaginatedCollection}
import scala.concurrent.{Await, Future}
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

  def index(
    org: String,
    teamKey: Option[String],
    hasTeam: Option[String],
    hasPlan: Option[String],
    hasGrade: Option[String],
    page: Int = 0
  ) = OrgAction.async { implicit request =>
    val filters = Filters(
      teamKey = lib.Filters.toOption(teamKey),
      hasTeam = lib.Filters.toOption(hasTeam),
      hasPlan = lib.Filters.toOption(hasPlan),
      hasGrade = lib.Filters.toOption(hasGrade)
    )

    for {
      incidents <- Api.instance.incidents.getByOrg(
        org = org,
        teamKey = filters.teamKey,
        hasTeam = filters.hasTeam.map(_.toInt > 0),
        hasPlan = filters.hasPlan.map(_.toInt > 0),
        hasGrade = filters.hasGrade.map(_.toInt > 0),
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
      teams <- Api.instance.teams.getByOrg(org, limit = Some(MaxTeams))
    } yield {
      val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
      Ok(views.html.incidents.index(request.org, filters, PaginatedCollection(page, incidents), teamsOrEmpty))
    }
  }

  def show(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    for {
      incident <- Api.instance.incidents.getByOrgAndId(org, id)
      plans <- Api.instance.Plans.getByOrg(org, incidentId = Some(id))
      meetings <- Api.instance.Meetings.getByOrg(org, incidentId = Some(id))
    } yield {
      incident match {
        case None => Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $id not found")
        case Some(i) => {
          Ok(views.html.incidents.show(request.org, i, plans.headOption, meetings))
        }
      }
    }
  }

  def postDeleteById(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    for {
      result <- Api.instance.incidents.deleteByOrgAndId(org, id)
    } yield {
      Redirect(routes.Incidents.index(org)).flashing("success" -> s"Incident $id deleted")
    }
  }

  def create(
    org: String,
    teamKey: Option[String] = None
  ) = OrgAction.async { implicit request =>
    for {
      teams <- Api.instance.teams.getByOrg(org, limit = Some(MaxTeams))
    } yield {
      val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
      val form = uiForm.fill(
        UiForm(
          summary = "",
          description = Some(util.ExampleIncident.description),
          teamKey = teamKey,
          severity = "",
          tags = ""
        )
      )
      Ok(views.html.incidents.create(request.org, form, teamsOrEmpty))
    }
  }

  def postCreate(org: String) = OrgAction.async { implicit request =>
    val boundForm = uiForm.bindFromRequest
    boundForm.fold (

      formWithErrors =>{
        for {
          teams <- Api.instance.teams.getByOrg(org, limit = Some(MaxTeams))
        } yield {
          val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
          Ok(views.html.incidents.create(request.org, formWithErrors, teamsOrEmpty))
        }
      },

      uiForm => {
        Api.instance.incidents.postByOrg(
          org = org,
          incidentForm = IncidentForm(
            summary = uiForm.summary,
            description = uiForm.description,
            teamKey = uiForm.teamKey,
            severity = Severity(uiForm.severity),
            tags = uiForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty)
          )
        ).map { incident =>
          Redirect(routes.Incidents.show(org, incident.id)).flashing("success" -> "Incident created")
        }.recover {
          case response: com.gilt.quality.error.ErrorsResponse => {
            Ok(views.html.incidents.create(request.org, boundForm, fetchTeamsOrEmpty(org), Some(response.errors.map(_.message).mkString("\n"))))
          }
        }
      }
    )
  }

  def edit(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    for {
      teams <- Api.instance.teams.getByOrg(org, limit = Some(MaxTeams))
      incidentOption <- Api.instance.incidents.getByOrgAndId(org, id)
    } yield {
      incidentOption match {
        case None => {
          Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $id not found")
        }
        case Some(incident: Incident) => {
          val form = uiForm.fill(
            UiForm(
              summary = incident.summary,
              description = incident.description,
              teamKey = incident.team.map(_.key),
              severity = incident.severity.toString,
              tags = incident.tags.mkString(" ")
            )
          )
          val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
          Ok(views.html.incidents.edit(request.org, incident, teamsOrEmpty, form))
        }
      }
    }
  }

  def postEdit(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, id).flatMap {
      case None => Future {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $id not found")
      }

      case Some(incident: Incident) => {
        val boundForm = uiForm.bindFromRequest
        boundForm.fold (

          formWithErrors => {
            for {
              teams <- Api.instance.teams.getByOrg(org, limit = Some(MaxTeams))
            } yield {
              val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
              Ok(views.html.incidents.edit(request.org, incident, teamsOrEmpty, formWithErrors))
            }
          },

          uiForm => {
            Api.instance.incidents.putByOrgAndId(
              org = org,
              id = incident.id,
              incidentForm = IncidentForm(
                summary = uiForm.summary,
                description = uiForm.description,
                teamKey = uiForm.teamKey,
                severity = Severity(uiForm.severity),
                tags = uiForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty)
              )
            ).map { r =>
              Redirect(routes.Incidents.show(org, incident.id)).flashing("success" -> "Incident updated")
            }.recover {
              case r: com.gilt.quality.error.ErrorsResponse => {
                Ok(views.html.incidents.create(request.org, boundForm, fetchTeamsOrEmpty(org), Some(r.errors.map(_.message).mkString("\n"))))
              }
            }
          }
        )
      }
    }
  }

  /**
    * If there are over MaxTeams, returns empty string. UI will
    * display a text field for team key in these cases.
    */
  private def fetchTeamsOrEmpty(org: String): Seq[Team] = {
    val teams = Await.result(
      Api.instance.teams.getByOrg(org, limit = Some(MaxTeams)),
      1000.millis
    )
    if (teams.size >= MaxTeams) {
      Seq.empty
    } else {
      teams
    }
  }

  case class UiForm(
    summary: String,
    description: Option[String],
    teamKey: Option[String],
    severity: String,
    tags: String
  )

  private val uiForm = Form(
    mapping(
      "summary" -> nonEmptyText,
      "description" -> optional(text),
      "teamKey" -> optional(text),
      "severity" -> nonEmptyText,
      "tags" -> text
    )(UiForm.apply)(UiForm.unapply)
  )

}
