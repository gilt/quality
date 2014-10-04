package db

import com.gilt.quality.models.{AgendaItem, Meeting, MeetingForm, Task}
import org.joda.time.DateTime
import anorm._
import anorm.ParameterValue._
import db.AnormHelper._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

object MeetingsDao {

  private val BaseQuery = """
    select id, scheduled_at,
           array_to_json(array(select row_to_json(agenda_items)
                                 from agenda_items
                                where agenda_items.deleted_at is null
                                  and agenda_items.meeting_id = meetings.id
                                order by agenda_items.incident_id))::varchar as agenda_items
      from meetings
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into meetings
    (scheduled_at, created_by_guid)
    values
    ({scheduled_at}, {user_guid}::uuid)
  """

  private val SoftDeleteAgendaItemsQuery = """
    update agenda_items
       set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now()
     where meeting_id = {meeting_id}
       and deleted_at is null
  """

  def create(user: User, form: MeetingForm): Meeting = {
    val id: Long = DB.withTransaction { implicit c =>
      SQL(InsertQuery).on(
        'scheduled_at -> form.scheduledAt,
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
      SQL(SoftDeleteAgendaItemsQuery).on('deleted_by_guid -> deletedBy.guid, 'meeting_id -> meeting.id).execute()
    }
  }

  def findById(id: Long): Option[Meeting] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(
    id: Option[Long] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Meeting] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and meetings.id = {id}" },
      Some("order by meetings.scheduled_at desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        val agendaItems = Json.parse(row[String]("agenda_items")).as[JsArray].value.map(_.as[JsObject]).map { json =>
          AgendaItem(
            id = (json \ "id").as[Long],
            incidentId = (json \ "incident_id").as[Long],
            task = Task((json \ "task").as[String])
          )
        }

        Meeting(
          id = row[Long]("id"),
          scheduledAt = row[DateTime]("scheduled_at"),
          agendaItems = agendaItems
        )
      }.toSeq
    }
  }

}
