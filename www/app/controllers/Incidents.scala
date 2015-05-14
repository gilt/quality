package controllers

import client.Api
import com.gilt.quality.v0.models.{Incident, IncidentForm, IncidentOrganizationChange, Organization, Severity, Team}
import com.gilt.quality.v0.errors.UnitResponse
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
  private val MaxOrgs = 1000

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
        limit = Pagination.DefaultLimit+1,
        offset = page * Pagination.DefaultLimit
      )
      teams <- Api.instance.teams.getByOrg(org, limit = MaxTeams)
    } yield {
      val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
      Ok(views.html.incidents.index(request.mainTemplate(), request.org, filters, PaginatedCollection(page, incidents), teamsOrEmpty))
    }
  }

  def show(
    org: String,
    id: Long,
    agendaItemsPage: Int = 0,
    meetingId: Option[Long] = None
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, id).flatMap { incident =>
      for {
        incident <- Api.instance.incidents.getByOrgAndId(org, id)
        plans <- Api.instance.Plans.getByOrg(org, incidentId = Some(id))
        agendaItems <- Api.instance.agendaItems.getByOrg(
          org = org,
          incidentId = Some(id),
          limit = Pagination.DefaultLimit+1,
          offset = agendaItemsPage * Pagination.DefaultLimit
        )
      } yield {
        val pagerOption = meetingId.flatMap { mid =>
          Await.result(
            Api.instance.meetings.getPagerByOrgAndIdAndIncidentId(request.org.key, mid, incident.id).map { r => Some(r) }.recover {
              case UnitResponse(404) => {
                None
              }
            },
            1000.millis
          )
        }

        Ok(
          views.html.incidents.show(
            request.mainTemplate(),
            request.org,
            incident,
            plans.headOption,
            PaginatedCollection(agendaItemsPage, agendaItems),
            pagerOption
          )
        )
      }
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $org/$id not found")
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
      teams <- Api.instance.teams.getByOrg(org, limit = MaxTeams)
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
      Ok(views.html.incidents.create(request.mainTemplate(), request.org, form, teamsOrEmpty))
    }
  }

  def postCreate(org: String) = OrgAction.async { implicit request =>
    val boundForm = uiForm.bindFromRequest
    boundForm.fold (

      formWithErrors =>{
        for {
          teams <- Api.instance.teams.getByOrg(org, limit = MaxTeams)
        } yield {
          val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
          Ok(views.html.incidents.create(request.mainTemplate(), request.org, formWithErrors, teamsOrEmpty))
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
            tags = Some(uiForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty))
          )
        ).map { incident =>
          Redirect(routes.Incidents.show(org, incident.id)).flashing("success" -> "Incident created")
        }.recover {
          case response: com.gilt.quality.v0.errors.ErrorsResponse => {
            Ok(views.html.incidents.create(request.mainTemplate(), request.org, boundForm, fetchTeamsOrEmpty(org), Some(response.errors.map(_.message).mkString("\n"))))
          }
        }
      }
    )
  }

  def edit(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, id).flatMap { incident =>
      for {
        teams <- Api.instance.teams.getByOrg(org, limit = MaxTeams)
      } yield {
        val form = uiForm.fill(
          UiForm(
            summary = incident.summary,
            description = incident.description,
            teamKey = incident.team.map(_.key),
            severity = incident.severity.toString,
            tags = incident.tags.getOrElse(Nil).mkString(" ")
          )
        )
        val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
        Ok(views.html.incidents.edit(request.mainTemplate(), request.org, incident, teamsOrEmpty, form))
      }
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $org/$id not found")
      }
    }
  }

  def postEdit(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, id).flatMap { incident =>
      val boundForm = uiForm.bindFromRequest
      boundForm.fold (

        formWithErrors => {
          for {
            teams <- Api.instance.teams.getByOrg(org, limit = MaxTeams)
          } yield {
            val teamsOrEmpty = if (teams.size >= MaxTeams) { Seq.empty } else { teams }
            Ok(views.html.incidents.edit(request.mainTemplate(), request.org, incident, teamsOrEmpty, formWithErrors))
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
              tags = Some(uiForm.tags.split(" +").map(_.trim).filter(t => !t.isEmpty))
            )
          ).map { r =>
            Redirect(routes.Incidents.show(org, incident.id)).flashing("success" -> "Incident updated")
          }.recover {
            case r: com.gilt.quality.v0.errors.ErrorsResponse => {
              val errors = r.errors.map(_.message).mkString("\n")
              Ok(views.html.incidents.create(request.mainTemplate(), request.org, boundForm, fetchTeamsOrEmpty(org), Some(errors)))
            }
          }
        }
      )
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $org/$id not found")
      }
    }

  }

  def move(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, id).map { incident =>
      val form = moveForm.fill(
        MoveForm(
          newOrganizationKey = ""
        )
      )

      Ok(views.html.incidents.move(request.mainTemplate(), request.org, incident, form, fetchOrgsToMove(request.org)))
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $org/$id not found")
      }
    }
  }

  def postMove(
    org: String,
    id: Long
  ) = OrgAction.async { implicit request =>
    Api.instance.incidents.getByOrgAndId(org, id).flatMap { incident =>
      val boundForm = moveForm.bindFromRequest
      boundForm.fold (

        formWithErrors => Future {
          Ok(views.html.incidents.move(request.mainTemplate(), request.org, incident, formWithErrors, fetchOrgsToMove(request.org)))
        },

        moveForm => {
          Api.instance.incidentOrganizationChanges.post(
            IncidentOrganizationChange(
              incidentId = incident.id,
              organizationKey = moveForm.newOrganizationKey
            )
          ).map { r =>
            Redirect(routes.Incidents.index(org)).flashing("success" -> s"Incident ${incident.id} moved to ${moveForm.newOrganizationKey}")
          }.recover {
            case r: com.gilt.quality.v0.errors.ErrorsResponse => {
              val errors = r.errors.map(_.message).mkString("\n")
              Ok(views.html.incidents.move(request.mainTemplate(), request.org, incident, boundForm, fetchOrgsToMove(request.org), Some(errors)))
            }
          }
        }
      )
    }.recover {
      case UnitResponse(404) => {
        Redirect(routes.Incidents.index(org)).flashing("warning" -> s"Incident $org/$id not found")
      }
    }
  }

  /**
    * If there are over MaxTeams, returns empty string. UI will
    * display a text field for team key in these cases.
    */
  private def fetchTeamsOrEmpty(org: String): Seq[Team] = {
    val teams = Await.result(
      Api.instance.teams.getByOrg(org, limit = MaxTeams),
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

  private def fetchOrgsToMove(currentOrg: Organization): Seq[Organization] = {
    val orgs = Await.result(
      Api.instance.organizations.get(limit = MaxOrgs),
      1000.millis
    )
    // TODO: Fetch all
    orgs.filter(_.key != currentOrg.key)
  }

  case class MoveForm(
    newOrganizationKey: String
  )

  private val moveForm = Form(
    mapping(
      "new_organization_key" -> nonEmptyText
    )(MoveForm.apply)(MoveForm.unapply)
  )

}
