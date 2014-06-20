package db

import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class Report(id: Long, incident_id: Long, body: String)

object Report {
  implicit val reportWrites = Json.writes[Report]
}

case class ReportForm(incident_id: Long, body: String)

object ReportForm {
  implicit val readsReportForm = Json.reads[ReportForm]
}

object ReportsDao {

  private val BaseQuery = """
    select id, incident_id, body
      from reports
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into reports
    (incident_id, body, created_by_guid, updated_by_guid)
    values
    ({incident_id}, {body}, {user_guid}::uuid, {user_guid}::uuid)
  """

  def create(user: User, form: ReportForm): Report = {
    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'incident_id -> form.incident_id,
        'body -> form.body,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create report")
    }
  }

  def softDelete(deletedBy: User, report: Report) {
    SoftDelete.delete("reports", deletedBy, report.id)
  }

  def findById(id: Long): Option[Report] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              incidentId: Option[Long] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[Report] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and reports.id = {id}" },
      incidentId.map { v => "and reports.incident_id = lower(trim({incident_id}))" },
      Some("order by reports.created_at desc, reports.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Report(
          id = row[Long]("id"),
          incident_id = row[Long]("incident_id"),
          body = row[String]("body")
        )
      }.toSeq
    }
  }

}
