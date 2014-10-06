package db

import com.gilt.quality.models.{Error, Incident, Organization, Plan, Severity, Team}
import com.gilt.quality.models.json._
import lib.Validation

import anorm._
import anorm.ParameterValue._
import AnormHelper._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import org.joda.time.DateTime

case class FullIncidentForm(
  org: Organization,
  form: IncidentForm
) {
  lazy val teamId: Option[Long] = {
    form.team_key.flatMap { key =>
      TeamsDao.lookupId(org, key)
    }
  }

  lazy val validate: Seq[Error] = {
    form.team_key match {
      case None => Seq.empty
      case Some(key) => {
        teamId match {
          case None => Validation.error(s"Team '$key' not found")
          case Some(_) => Seq.empty
        }
      }
    }
  }

}

case class IncidentForm(
  team_key: Option[String],
  severity: String,
  summary: String,
  description: Option[String] = None,
  tags: Option[Seq[String]] = None
)

object IncidentForm {
  implicit val readsIncidentForm = Json.reads[IncidentForm]
}

object IncidentsDao {

  private val BaseQuery = """
    select incidents.id,
           organizations.key as organization_key, 
           organizations.name as organization_name,
           teams.key as team_key,
           incidents.severity,
           incidents.summary,
           incidents.description,
           incidents.created_at,
           plans.id as plan_id,
           plans.body as plan_body,
           plans.created_at as plan_created_at,
           grades.score as grade
      from incidents
      join organizations on organizations.deleted_at is null and organizations.id = incidents.organization_id
      left join teams on teams.deleted_at is null and teams.id = incidents.team_id
      left join plans on plans.deleted_at is null and plans.incident_id = incidents.id
      left join grades on grades.deleted_at is null and grades.plan_id = plans.id
     where incidents.deleted_at is null
  """

  private val InsertQuery = """
    insert into incidents
    (organization_id, team_id, severity, summary, description, created_by_guid, updated_by_guid)
    values
    ({organization_id}, {team_id}, {severity}, {summary}, {description}, {user_guid}::uuid, {user_guid}::uuid)
  """

  private val UpdateQuery = """
    update incidents
       set team_id = {team_id},
           severity = {severity},
           summary = {summary},
           description = {description},
           updated_at = now(),
           updated_by_guid = {user_guid}::uuid
     where id = {id}
  """

  def create(user: User, fullForm: FullIncidentForm): Incident = {
    assert(fullForm.validate.isEmpty, fullForm.validate.map(_.message).mkString(" "))

    val orgId = OrganizationsDao.lookupId(fullForm.org.key).getOrElse {
      sys.error(s"Could not find organizations with key[${fullForm.org.key}]")
    }

    val id: Long = DB.withTransaction { implicit c =>
      val id = SQL(InsertQuery).on(
        'organization_id -> orgId,
        'team_id -> fullForm.teamId,
        'severity -> fullForm.form.severity,
        'summary -> fullForm.form.summary,
        'description -> fullForm.form.description,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))

      fullForm.form.tags.foreach { tags =>
        IncidentTagsDao.doUpdate(c, user, id, Seq.empty, tags)
      }

      id
    }

    global.Actors.mainActor ! actors.IncidentMessage.SyncOne(id)

    findById(id).getOrElse {
      sys.error("Failed to create incident")
    }
  }

  def update(user: User, incident: Incident, fullForm: FullIncidentForm): Incident = {
    assert(fullForm.validate.isEmpty, fullForm.validate.map(_.message).mkString(" "))
    assert(
      incident.organization.key == fullForm.org.key,
      s"Incident[${incident.id}] belongs to org[${incident.organization.key}] and not[${fullForm.org.key}]"
    )

    DB.withTransaction { implicit c =>
      SQL(UpdateQuery).on(
        'id -> incident.id,
        'team_id -> fullForm.teamId,
        'severity -> fullForm.form.severity,
        'summary -> fullForm.form.summary,
        'description -> fullForm.form.description,
        'user_guid -> user.guid
      ).executeUpdate()

      IncidentTagsDao.doUpdate(c, user, incident.id, incident.tags, fullForm.form.tags.getOrElse(Seq.empty))
    }

    global.Actors.mainActor ! actors.IncidentMessage.SyncOne(incident.id)

    findById(incident.id).getOrElse {
      sys.error("Failed to update incident")
    }
  }

  def softDelete(deletedBy: User, incident: Incident) {
    SoftDelete.delete("incidents", deletedBy, incident.id)
  }

  def findById(
    id: Long
  ): Option[Incident] = {
    findAll(id = Some(id), limit = 1).headOption.map { i => findDetails(i) }
  }

  def findByOrganizationAndId(
    org: Organization,
    id: Long
  ): Option[Incident] = {
    findAll(orgKey = Some(org.key), id = Some(id), limit = 1).headOption.map { i => findDetails(i) }
  }

  private def findDetails(incident: Incident): Incident = {
    incident.copy(tags = IncidentTagsDao.findAll(incidentId = Some(incident.id)).map(_.tag))
  }

  def findAll(
    orgKey: Option[String] = None,
    id: Option[Long] = None,
    teamKey: Option[String] = None,
    hasTeam: Option[Boolean] = None,
    hasPlan: Option[Boolean] = None,
    hasGrade: Option[Boolean] = None,
    severity: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Incident] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      orgKey.map { v => "and incidents.organization_id = (select id from organizations where deleted_at is null and key = {org_key})" },
      id.map { v => "and incidents.id = {id}" },
      teamKey.map { v => "and incidents.team_id = (select id from teams where deleted_at is null and key = lower(trim({team_key})))" },
      hasTeam.map { v =>
        v match {
          case true => "and teams.id is not null"
          case false => "and teams.id is null"
        }
      },
      hasPlan.map { v =>
        v match {
          case true => "and plans.id is not null"
          case false => "and plans.id is null"
        }
      },
      hasGrade.map { v =>
        v match {
          case true => "and grades.id is not null"
          case false => "and grades.id is null"
        }
      },
      severity.map { v => "and incidents.severity = {severity}" },
      Some("order by incidents.created_at desc, incidents.id desc"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      orgKey.map { v => NamedParameter("org_key", toParameterValue(v)) },
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      teamKey.map { v => NamedParameter("team_key", toParameterValue(v)) },
      severity.map { v => NamedParameter("severity", toParameterValue(severity)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        val incidentId = row[Long]("id")

        val plan = row[Option[Long]]("plan_id").map { planId =>
          Plan(
            id = planId,
            incidentId = incidentId,
            body = row[String]("plan_body"),
            grade = row[Option[Int]]("grade"),
            createdAt = row[DateTime]("plan_created_at")
          )
        }

        Incident(
          id = incidentId,
          organization = Organization(
            key = row[String]("organization_key"),
            name = row[String]("organization_name")
          ),
          team = row[Option[String]]("team_key").map { team_key =>
            Team(
              key = team_key,
              organization = Organization(
                key = row[String]("organization_key"),
                name = row[String]("organization_name")
              )
            )
          },
          severity = Severity(row[String]("severity")),
          summary = row[String]("summary"),
          description = row[Option[String]]("description"),
          tags = Seq.empty,
          plan = plan,
          createdAt = row[DateTime]("created_at")
        )
      }.toSeq
    }
  }

}
