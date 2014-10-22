package db

import com.gilt.quality.models.{Error, Incident, Organization, Plan, PlanForm, User}
import lib.Validation
import anorm._
import anorm.ParameterValue._
import AnormHelper._
import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class FullPlanForm(
  org: Organization,
  incident: Incident,
  form: PlanForm
) {

  def validate(): Seq[Error] = {
    if (form.body.trim.isEmpty) {
      Validation.error("Plan body cannot be empty")
    } else {
      Seq.empty
    }
  }

}

object PlansDao {

  private val BaseQuery = """
    select plans.id,
           plans.incident_id,
           plans.body,
           plans.created_at,
           grades.score as grade_score
      from plans
      join incidents on incidents.deleted_at is null and incidents.id = plans.incident_id
      left join teams on teams.deleted_at is null and teams.id = incidents.team_id
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
       set body = {body},
           updated_at = now(),
           updated_by_guid = {user_guid}::uuid
     where id = {id}
  """

  def create(user: User, fullForm: FullPlanForm): Plan = {
    val errors = fullForm.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))

    val id: Long = DB.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'incident_id -> fullForm.incident.id,
        'body -> fullForm.form.body,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    global.Actors.mainActor ! actors.MainActor.PlanCreated(id)

    findByOrganizationAndId(fullForm.org, id).getOrElse {
      sys.error("Failed to create plan")
    }
  }

  def update(user: User, plan: Plan, fullForm: FullPlanForm): Plan = {
    val errors = fullForm.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))
    assert(plan.incidentId == fullForm.form.incidentId, s"Cannot change incident id for plan[${plan.id}]")

    DB.withConnection { implicit c =>
      SQL(UpdateQuery).on(
        'id -> plan.id,
        'body -> fullForm.form.body,
        'user_guid -> user.guid
      ).executeUpdate()
    }

    global.Actors.mainActor ! actors.MainActor.PlanUpdated(plan.id)

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

  def findByOrganizationAndId(
    org: Organization,
    id: Long
  ): Option[Plan] = {
    findAll(org = Some(org), id = Some(id), limit = 1).headOption
  }

  def findAll(
    org: Option[Organization] = None,
    id: Option[Long] = None,
    incidentId: Option[Long] = None,
    teamKey: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Plan] = {
    val orgId = org match {
      case None => None
      case Some(o) => {
        Some(
          OrganizationsDao.lookupId(o.key).getOrElse {
            sys.error("Organization ID not found for key[${org.key}]")
          }
        )
      }
    }

    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and plans.id = {id}" },
      org.map { v => "and incidents.organization_id = {organization_id}" },
      incidentId.map { v => "and plans.incident_id = {incident_id}" },
      teamKey.map {v => "and teams.key = {team_key}"},
      Some("order by plans.created_at desc, plans.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      orgId.map { v => NamedParameter("organization_id", toParameterValue(v)) },
      incidentId.map { v => NamedParameter("incident_id", toParameterValue(v)) },
      teamKey.map {v => NamedParameter("team_key", toParameterValue(v))}
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Plan(
          id = row[Long]("id"),
          incidentId = row[Long]("incident_id"),
          body = row[String]("body"),
          grade = row[Option[Int]]("grade_score"),
          createdAt = row[DateTime]("created_at")
        )
      }.toSeq
    }
  }

}
