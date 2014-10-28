package controllers

import db.{AgendaItemsDao, FullAgendaItemForm, MeetingsDao}
import com.gilt.quality.models.{AgendaItemForm, User}
import lib.Validation
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object AgendaItems extends Controller with AgendaItems

trait AgendaItems {
  this: Controller =>

  def getByOrg(
    org: String,
    id: Option[Long],
    meetingId: Option[Long],
    incidentId: Option[Long],
    teamKey: Option[String],
    isAdjourned: Option[Boolean],
    task: Option[com.gilt.quality.models.Task],
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val items = AgendaItemsDao.findAll(
      org = Some(request.org),
      meetingId = meetingId,
      id = id,
      incidentId = incidentId,
      teamKey = teamKey,
      isAdjourned = isAdjourned,
      task = task,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(items))
  }

  def getByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    AgendaItemsDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(item) => Ok(Json.toJson(item))
    }
  }

  def deleteByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    AgendaItemsDao.findByOrganizationAndId(request.org, id).map { item =>
      AgendaItemsDao.softDelete(request.user, item)
    }
    NoContent
  }

  def postByOrg(
    org: String
  ) = OrgAction(parse.json) { request =>
    request.body.validate[AgendaItemForm] match {
      case e: JsError => {
        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[AgendaItemForm] => {
        val form = FullAgendaItemForm(request.org, s.get)
        form.validate match {
          case Nil => {
            val org = AgendaItemsDao.create(request.user, form)
            Created(Json.toJson(org))
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

}
