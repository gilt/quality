package db

import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class Incident(id: Long, team_key: String, severity: Severity, summary: String, description: Option[String])

object Incident {
  implicit val incidentWrites = Json.writes[Incident]
}

case class IncidentForm(team_key: String, severity: String, summary: String, description: Option[String])

object IncidentForm {
  implicit val readsIncidentForm = Json.reads[IncidentForm]
}

object IncidentsDao {

  private val BaseQuery = """
    select id, team_key, severity, summary, description
      from incidents
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into incidents
    (team_key, severity, summary, description, created_by_guid, updated_by_guid)
    values
    ({team_key}, {severity}, {summary}, {description}, {user_guid}::uuid, {user_guid}::uuid)
    returning id
  """

  def create(user: User, form: IncidentForm): Incident = {
    val severity = Severity.fromString(form.severity).getOrElse {
      sys.error(s"Invalid severity key[%s]".format(form.severity))
    }

    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'team_key -> form.team_key,
        'severity -> severity.key,
        'summary -> form.summary,
        'description -> form.description,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create incident")
    }
  }

  def softDelete(deletedBy: User, incident: Incident) {
    SoftDelete.delete("incidents", deletedBy, incident.id)
  }

  def findById(id: Long): Option[Incident] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              teamKey: Option[String] = None,
              severity: Option[String] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[Incident] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and incidents.id = {id}" },
      teamKey.map { v => "and incidents.team_key = lower(trim({team_key}))" },
      severity.map { v => "and incidents.severity = {severity}" },
      Some("order by incidents.created_at desc, incidents.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      teamKey.map { v => NamedParameter("team_key", toParameterValue(v)) },
      severity.map { v => NamedParameter("severity", toParameterValue(severity)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Incident(
          id = row[Long]("id"),
          team_key = row[String]("team_key"),
          severity = Severity.fromString(row[String]("severity")).getOrElse {
            sys.error(s"Invalid severity key[%s]".format(row[String]("severity")))
          },
          summary = row[String]("summary"),
          description = row[Option[String]]("description")
        )
      }.toSeq
    }
  }

}
