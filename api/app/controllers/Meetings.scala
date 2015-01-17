package controllers

import com.gilt.quality.v0.models.{AdjournForm, Error, Meeting, MeetingPager, MeetingForm, User}
import com.gilt.quality.v0.models.json._

import play.api.mvc._
import play.api.libs.json._
import java.util.UUID
import db.{AgendaItemsDao, FullMeetingForm, MeetingsDao, OrganizationsDao}
import lib.Validation

object Meetings extends Controller {

  def getByOrg(
    org: String,
    id: Option[Long],
    incidentId: Option[Long],
    agendaItemId: Option[Long],
    isUpcoming: Option[Boolean] = None,
    isAdjourned: Option[Boolean] = None,
    limit: Int = 25,
    offset: Int = 0
  ) = OrgAction { request =>
    val meetings = MeetingsDao.findAll(
      org = Some(request.org),
      id = id,
      incidentId = incidentId,
      agendaItemId = agendaItemId,
      isUpcoming = isUpcoming,
      isAdjourned = isAdjourned,
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
  ) = MeetingAction { request =>
    val pager = AgendaItemsDao.findAll(
      meetingId = Some(id),
      incidentId = Some(incidentId),
      limit = 1
    ).headOption match {
      case None => MeetingPager(meeting = request.meeting)
      case Some(item) => MeetingsDao.findPager(request.meeting, item)
    }
    Ok(Json.toJson(pager))
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

  def postAdjournByOrgAndId(
    org: String,
    id: Long
  ) = MeetingAction(parse.json) { request =>
    request.meeting.adjournedAt match {
      case None => {
        request.body.validate[AdjournForm] match {
          case e: JsError => Conflict(Json.toJson(Validation.invalidJson(e)))
          case s: JsSuccess[AdjournForm] => {
            val updated = MeetingsDao.adjourn(request.user, request.meeting, s.get)
            Ok(Json.toJson(updated))
          }
        }
      }
      case Some(_) => {
        Conflict(Json.toJson(Validation.error("This meeting has previously been adjourned")))
      }
    }
  }

}
