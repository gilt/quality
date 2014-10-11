package db

import com.gilt.quality.models.{AgendaItem, Meeting, MeetingForm, Organization, Task}
import org.joda.time.DateTime
import anorm._
import anorm.ParameterValue._
import db.AnormHelper._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class FullMeetingForm(
  org: Organization,
  form: MeetingForm
)

object MeetingsDao {

  private val BaseQuery = """
    select id, scheduled_at
      from meetings
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into meetings
    (organization_id, scheduled_at, created_by_guid)
    values
    ({organization_id}, {scheduled_at}, {user_guid}::uuid)
  """

  def create(user: User, fullForm: FullMeetingForm): Meeting = {
    val orgId = OrganizationsDao.lookupId(fullForm.org.key).getOrElse {
      sys.error(s"Could not find organizations with key[${fullForm.org.key}]")
    }

    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'organization_id -> orgId,
        'scheduled_at -> fullForm.form.scheduledAt,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create meeting")
    }
  }

  def softDelete(deletedBy: User, meeting: Meeting) {
    DB.withTransaction { implicit c =>
      SoftDelete.delete(c, "meetings", deletedBy, meeting.id)
      AgendaItemsDao.softDeleteAllForMeeting(c, deletedBy, meeting)
    }
  }

  def findById(id: Long): Option[Meeting] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findByOrganizationAndId(org: Organization, id: Long): Option[Meeting] = {
    findAll(org = Some(org), id = Some(id), limit = 1).headOption
  }

  def findAll(
    org: Option[Organization] = None,
    id: Option[Long] = None,
    incidentId: Option[Long] = None,
    scheduledAt: Option[DateTime] = None,
    isUpcoming: Option[Boolean] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Meeting] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      org.map { v => "and meetings.organization_id = (select id from organizations where deleted_at is null and key = {org_key})" },
      id.map { v => "and meetings.id = {id}" },
      incidentId.map { v => "and meetings.id in (select meeting_id from agenda_items where deleted_at is null and incident_id = {incident_id})" },
      scheduledAt.map { v => "and date_trunc('minute', meetings.scheduled_at) = date_trunc('minute', {scheduled_at}::timestamptz)" },
      isUpcoming.map { v =>
        v match {
          case true => "and meetings.scheduled_at > now()"
          case false => "and meetings.scheduled_at <= now()"
        }
      },
      Some("order by meetings.scheduled_at desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")


    val bind = Seq(
      org.map { v => NamedParameter("org_key", toParameterValue(v.key)) },
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) },
      scheduledAt.map { v => NamedParameter("scheduled_at", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Meeting(
          id = row[Long]("id"),
          scheduledAt = row[DateTime]("scheduled_at")
        )
      }.toSeq
    }
  }

}
