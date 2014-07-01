package db

import quality.models.{ Error, Report }
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class ReportWithId(id: Long, incident_id: Long, body: String, grade: Option[Long])

object ReportWithId {
  implicit val reportWithIdWrites = Json.writes[ReportWithId]
}

object ReportValidator {

  // TODO
  def validate(report: Report): Seq[Error] = {
    Seq.empty
  }

}

object ReportsDao {

  private val BaseQuery = """
    select id, incident_id, body, grades.score as grade
      from reports
      left join grades on grades.report_id = reports.id and grade.deleted_at is null
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into reports
    (incident_id, body, created_by_guid, updated_by_guid)
    values
    ({incident_id}, {body}, {user_guid}::uuid, {user_guid}::uuid)
  """

  def create(user: User, form: Report): ReportWithId = {
    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'incident_id -> form.incident.id,
        'body -> form.body,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create report")
    }
  }

  def update(user: User, report: ReportWithId, form: Report): ReportWithId = {
    sys.error("TODO")
  }

  def softDelete(deletedBy: User, report: ReportWithId) {
    SoftDelete.delete("reports", deletedBy, report.id)
  }

  def findById(id: Long): Option[ReportWithId] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              incidentId: Option[Long] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[ReportWithId] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and reports.id = {id}" },
      incidentId.map { v => "and reports.incident_id = {incident_id}" },
      Some("order by reports.created_at desc, reports.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        ReportWithId(
          id = row[Long]("id"),
          incident_id = row[Long]("incident_id"),
          body = row[String]("body"),
          grade = row[Option[Long]]("grade")
        )
      }.toSeq
    }
  }

}
