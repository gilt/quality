package db

import quality.models.{ Error, Plan }
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class PlanForm(incident_id: Long, body: String, grade: Option[Int] = None) {

  // TODO
  def validate(): Seq[Error] = {
    Seq.empty
  }

}

object PlanForm {
  implicit val planFormReads = Json.reads[PlanForm]
}

object PlansDao {

  private val BaseQuery = """
    select plans.id,
           plans.incident_id,
           plans.body,
           grades.score as grade_score
      from plans
      join incidents on incidents.deleted_at is null and incidents.id = plans.incident_id
      join teams on teams.deleted_at is null and teams.id = incidents.team_id
      left join grades on grades.deleted_at is null and grades.plan_id = plans.id
     where plans.deleted_at is null
  """

  private val InsertQuery = """
    insert into plans
    (incident_id, body, created_by_guid, updated_by_guid)
    values
    ({incident_id}, {body}, {user_guid}::uuid, {user_guid}::uuid)
  """

  private val UpdateQuery = """
    update plans
       set incident_id = {incident_id},
           body = {body},
           updated_at = now(),
           updated_by_guid = {user_guid}::uuid
     where id = {id}
  """

  def create(user: User, form: PlanForm): Plan = {
    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'incident_id -> form.incident_id,
        'body -> form.body,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create plan")
    }
  }

  def update(user: User, plan: Plan, form: PlanForm): Plan = {
    DB.withConnection { implicit c =>
      SQL(UpdateQuery).on(
        'id -> plan.id,
        'incident_id -> form.incident_id,
        'body -> form.body,
        'user_guid -> user.guid
      ).executeUpdate()
    }

    findById(plan.id).getOrElse {
      sys.error("Failed to update plan")
    }
  }

  def softDelete(deletedBy: User, plan: Plan) {
    SoftDelete.delete("plans", deletedBy, plan.id)
  }

  def findById(id: Long): Option[Plan] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(id: Option[Long] = None,
              incidentId: Option[Long] = None,
              teamKey: Option[String] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[Plan] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and plans.id = {id}" },
      incidentId.map { v => "and plans.incident_id = {incident_id}" },
      teamKey.map {v => "and teams.key = {team_key}"},
      Some("order by plans.created_at desc, plans.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) },
      teamKey.map {v => NamedParameter("team_key", v)}
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Plan(
          id = row[Long]("id"),
          incidentId = row[Long]("incident_id"),
          body = row[String]("body"),
          grade = row[Option[Int]]("grade_score")
        )
      }.toSeq
    }
  }

}
