package controllers

import com.gilt.quality.models.{Error, Meeting, MeetingForm, User}
import com.gilt.quality.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{FullMeetingForm, MeetingsDao, OrganizationsDao}
import lib.Validation

object Meetings extends Controller {

  def getByOrg(
    org: String,
    id: Option[Long],
    incidentId: Option[Long],
    agendaItemId: Option[Long],
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val meetings = MeetingsDao.findAll(
      org = Some(request.org),
      id = id,
      incidentId = incidentId,
      agendaItemId = agendaItemId,
      limit = limit,
      offset = offset
    )

    Ok(Json.toJson(meetings))
  }

  def getByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    MeetingsDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(m: Meeting) => Ok(Json.toJson(m))
    }
  }

  def getPagerByOrgAndIdAndIncidentId(
    org: String,
    id: Long,
    incidentId: Long
  ) = OrgAction { request =>
    MeetingsDao.findByOrganizationAndId(request.org, id) match {
      case None => NotFound
      case Some(m: Meeting) => {
        val pager = MeetingsDao.findPager(m, incidentId)
        Ok(Json.toJson(pager))
      }
    }
  }

  def postByOrg(org: String) = OrgAction(parse.json) { request =>
    request.body.validate[MeetingForm] match {
      case e: JsError => Conflict(Json.toJson(Validation.invalidJson(e)))

      case s: JsSuccess[MeetingForm] => {
        val form = FullMeetingForm(request.org, s.get)
        val meeting = MeetingsDao.create(request.user, form)
        Created(Json.toJson(meeting)).withHeaders(LOCATION -> routes.Meetings.getByOrgAndId(request.org.key, meeting.id).url)
      }
    }
  }

  def deleteByOrgAndId(
    org: String,
    id: Long
  ) = OrgAction { request =>
    MeetingsDao.findByOrganizationAndId(request.org, id).foreach { m =>
      MeetingsDao.softDelete(request.user, m)
    }
    NoContent
  }

}
