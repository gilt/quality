package db

import com.gilt.quality.models.{AgendaItem, AgendaItemForm, IncidentSummary, Meeting, Severity, Task}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class AgendaItemFullForm(
  meeting: Meeting,
  form: AgendaItemForm
)

object AgendaItemsDao {

  private val BaseQuery = """
    select agenda_items.id, agenda_items,task,
           incidents.id as incident_id,
           incidents.severity as incident_severity,
           incidents.summary as incident_summary
      from agenda_items
      join incidents on incidents.id = agenda_items.incident_id and incidents.deleted_at is null
     where agenda_items.deleted_at is null
  """

  private val InsertQuery = """
    insert into agenda_items
    (meeting_id, task, incident_id, created_by_guid)
    values
    ({meeting_id}, {task}, {incident_id}, {user_guid}::uuid)
  """

  private val SoftDeleteAgendaItemsQuery = """
    update agenda_items
       set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now()
     where meeting_id = {meeting_id}
       and deleted_at is null
  """

  def create(user: User, fullForm: AgendaItemFullForm): AgendaItem = {
    val id: Long = DB.withTransaction { implicit c =>
      SQL(InsertQuery).on(
        'meeting_id -> fullForm.meeting.id,
        'task -> fullForm.form.task.toString,
        'incident_id -> fullForm.form.incidentId,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create agenda item")
    }
  }

  def softDelete(deletedBy: User, agendaItem: AgendaItem) {
    SoftDelete.delete("agenda_items", deletedBy, agendaItem.id)
  }

  private[db] def softDeleteAllForMeeting(
    implicit c: java.sql.Connection,
    deletedBy: User,
    meeting: Meeting
  ) {
    SQL(SoftDeleteAgendaItemsQuery).on('deleted_by_guid -> deletedBy.guid, 'meeting_id -> meeting.id).execute()
  }

  def findById(id: Long): Option[AgendaItem] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(
    id: Option[Long] = None,
    meetingId: Option[Long] = None,
    task: Option[Task] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[AgendaItem] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and agenda_items.id = {id}" },
      meetingId.map { v => "and agenda_items.meeting_id = {meeting_id}" },
      task.map { v => "and agenda_items.task = {task}" },
      Some("order by agenda_items.incident_id"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      meetingId.map { v => NamedParameter("meeting_id", toParameterValue(v)) },
      task.map { v => NamedParameter("task", toParameterValue(task.toString)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        AgendaItem(
          id = row[Long]("id"),
          task = Task(row[String]("task")),
          incident = IncidentSummary(
            id = row[Long]("incident_id"),
            severity = Severity(row[String]("incident_severity")),
            summary = row[String]("incident_summary")
          )
        )
      }.toSeq
    }
  }

}
