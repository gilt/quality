package db

import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import java.sql.Connection

case class IncidentTag(id: Long, incident_id: Long, tag: String)

object IncidentTag {
  implicit val incidentTagWrites = Json.writes[IncidentTag]
}

case class IncidentTagForm(incident_id: Long, tag: String)

object IncidentTagForm {
  implicit val readsIncidentTagForm = Json.reads[IncidentTagForm]
}

object IncidentTagsDao {

  private val BaseQuery = """
    select id, incident_id, tag
      from incident_tags
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into incident_tags
    (incident_id, tag, created_by_guid, updated_by_guid)
    values
    ({incident_id}, {tag}, {user_guid}::uuid, {user_guid}::uuid)
  """

  private val DeleteTagQuery = """
    update incident_tags
       set deleted_at = now(), deleted_by_guid = {user_guid}::uuid
     where incident_id = {incident_id}
       and tag = {tag}
  """

  def create(user: User, form: IncidentTagForm): IncidentTag = {
    val id = DB.withConnection { implicit c =>
      doInsert(c, user, form)
    }

    findById(id).getOrElse {
      sys.error("Failed to create incidentTag")
    }
  }

  def softDelete(deletedBy: User, incidentTag: IncidentTag) {
    SoftDelete.delete("incident_tags", deletedBy, incidentTag.id)
  }

  private[db] def doUpdate(implicit conn: Connection, user: User, incidentId: Long, from: Seq[String], to: Seq[String]) {
    to.filter(tag => !from.contains(tag)).foreach { tag =>
      doInsert(conn, user, IncidentTagForm(incident_id = incidentId, tag = tag))
    }

    from.filter(tag => !to.contains(tag)).foreach { tag =>
      SQL(DeleteTagQuery).on(
        'incident_id -> incidentId,
        'tag -> tag,
        'user_guid -> user.guid
      ).execute()
    }
  }

  private[this] def doInsert(implicit conn: Connection, user: User, form: IncidentTagForm): Long = {
    SQL(InsertQuery).on(
      'incident_id -> form.incident_id,
      'tag -> form.tag,
      'user_guid -> user.guid,
      'user_guid -> user.guid
    ).executeInsert().getOrElse(sys.error("Missing id"))
  }

  def findById(id: Long): Option[IncidentTag] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              incidentId: Option[Long] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[IncidentTag] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and incident_tags.id = {id}" },
      incidentId.map { v => "and incident_tags.incident_id = {incident_id}" },
      Some("order by incident_tags.created_at, incident_tags.id"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        IncidentTag(
          id = row[Long]("id"),
          incident_id = row[Long]("incident_id"),
          tag = row[String]("tag")
        )
      }.toSeq
    }
  }

}
