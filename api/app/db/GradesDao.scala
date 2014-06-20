package db

import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class Grade(id: Long, report_id: Long, grade: Int) {
  require(grade >= 0 && grade <= 100, "Grade must be between 0-100 and not[%s]".format(grade))
}

object Grade {
  implicit val reportWrites = Json.writes[Grade]
}

case class GradeForm(report_id: Long, grade: Int)

object GradeForm {
  implicit val readsGradeForm = Json.reads[GradeForm]
}

object GradesDao {

  private val BaseQuery = """
    select id, report_id, grade
      from grades
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into grades
    (report_id, grade, created_by_guid, updated_by_guid)
    values
    ({report_id}, {grade}, {user_guid}::uuid, {user_guid}::uuid)
  """

  def create(user: User, form: GradeForm): Grade = {
    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'report_id -> form.report_id,
        'grade -> form.grade,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create report")
    }
  }

  def softDelete(deletedBy: User, report: Grade) {
    SoftDelete.delete("grades", deletedBy, report.id)
  }

  def findById(id: Long): Option[Grade] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              reportId: Option[Long] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[Grade] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and grades.id = {id}" },
      reportId.map { v => "and grades.report_id = report_id" },
      Some("order by grades.created_at desc, grades.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      reportId.map { v => NamedParameter("report_id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Grade(
          id = row[Long]("id"),
          report_id = row[Long]("report_id"),
          grade = row[Int]("grade")
        )
      }.toSeq
    }
  }

}
