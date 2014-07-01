package db

import quality.models.{ Error, Plan }
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class PlanWithId(id: Long, incident_id: Long, body: String, grade: Option[Long])

object PlanWithId {
  implicit val planWithIdWrites = Json.writes[PlanWithId]
}

object PlanValidator {

  // TODO
  def validate(plan: Plan): Seq[Error] = {
    Seq.empty
  }

}

object PlansDao {

  private val BaseQuery = """
    select plans.id, plans.incident_id, plans.body, grades.score as grade
      from plans
      left join grades on grades.plan_id = plans.id and grades.deleted_at is null
     where plans.deleted_at is null
  """

  private val InsertQuery = """
    insert into plans
    (incident_id, body, created_by_guid, updated_by_guid)
    values
    ({incident_id}, {body}, {user_guid}::uuid, {user_guid}::uuid)
  """

  def create(user: User, form: Plan): PlanWithId = {
    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'incident_id -> form.incident.id,
        'body -> form.body,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create plan")
    }
  }

  def update(user: User, plan: PlanWithId, form: Plan): PlanWithId = {
    sys.error("TODO")
  }

  def softDelete(deletedBy: User, plan: PlanWithId) {
    SoftDelete.delete("plans", deletedBy, plan.id)
  }

  def findById(id: Long): Option[PlanWithId] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              incidentId: Option[Long] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[PlanWithId] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and plans.id = {id}" },
      incidentId.map { v => "and plans.incident_id = {incident_id}" },
      Some("order by plans.created_at desc, plans.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        PlanWithId(
          id = row[Long]("id"),
          incident_id = row[Long]("incident_id"),
          body = row[String]("body"),
          grade = row[Option[Long]]("grade")
        )
      }.toSeq
    }
  }

}
