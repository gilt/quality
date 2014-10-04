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
    select id, scheduled_at
      from meetings
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into meetings
    (scheduled_at, created_by_guid)
    values
    ({scheduled_at}, {user_guid}::uuid)
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
      AgendaItemsDao.softDeleteAllForMeeting(c, deletedBy, meeting)
    }
  }

  def findById(id: Long): Option[Meeting] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(
    id: Option[Long] = None,
    scheduledAt: Option[DateTime] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Meeting] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and meetings.id = {id}" },
      scheduledAt.map { v => "and date_trunc('minute', meetings.scheduled_at) = date_trunc('minute', {scheduled_at}::timestamptz)" },
      Some("order by meetings.scheduled_at desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
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
