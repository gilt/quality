package db

import com.gilt.quality.v0.models.User
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

  private val SoftDeleteByPlanIdQuery = """
    update grades set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now() where plan_id = {plan_id} and deleted_at is null
  """

  def upsert(user: User, form: GradeForm): Grade = {
    val id = DB.withTransaction { implicit c =>
      SQL(SoftDeleteByPlanIdQuery).on('deleted_by_guid -> user.guid, 'plan_id -> form.plan_id).execute()
      create(c, user, form)
    }

    findById(id).getOrElse {
      sys.error("Failed to create plan")
    }
  }

  private[db] def create(implicit c: java.sql.Connection, user: User, form: GradeForm): Long = {
    SQL(InsertQuery).on(
      'plan_id -> form.plan_id,
      'score -> form.score,
      'user_guid -> user.guid,
      'user_guid -> user.guid
    ).executeInsert().getOrElse(sys.error("Missing id"))
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
