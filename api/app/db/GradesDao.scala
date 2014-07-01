package db

import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class Grade(id: Long, plan_id: Long, score: Int) {
  require(score >= 0 && score <= 100, "Score must be between 0-100 and not[%s]".format(score))
}

object Grade {
  implicit val planWrites = Json.writes[Grade]
}

case class GradeForm(plan_id: Long, score: Int)

object GradeForm {
  implicit val readsGradeForm = Json.reads[GradeForm]
}

object GradesDao {

  private val BaseQuery = """
    select id, plan_id, score
      from grades
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into grades
    (plan_id, score, created_by_guid, updated_by_guid)
    values
    ({plan_id}, {score}, {user_guid}::uuid, {user_guid}::uuid)
  """

  def create(user: User, form: GradeForm): Grade = {
    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'plan_id -> form.plan_id,
        'score -> form.score,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create plan")
    }
  }

  def softDelete(deletedBy: User, plan: Grade) {
    SoftDelete.delete("grades", deletedBy, plan.id)
  }

  def findById(id: Long): Option[Grade] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              planId: Option[Long] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[Grade] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and grades.id = {id}" },
      planId.map { v => "and grades.plan_id = {plan_id}" },
      Some("order by grades.created_at desc, grades.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      planId.map { v => NamedParameter("plan_id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Grade(
          id = row[Long]("id"),
          plan_id = row[Long]("plan_id"),
          score = row[Int]("score")
        )
      }.toSeq
    }
  }

}
