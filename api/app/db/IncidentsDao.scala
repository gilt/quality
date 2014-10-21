package db

import com.gilt.quality.models.{Error, Incident, IncidentForm, Organization, Plan, Severity, Team, User}
import com.gilt.quality.models.json._
import lib.Validation

import anorm._
import anorm.ParameterValue._
import AnormHelper._
import play.api.db._
import play.api.Play.current
import org.joda.time.DateTime

case class FullIncidentForm(
  org: Organization,
  form: IncidentForm
) {
  lazy val orgId = OrganizationsDao.lookupId(org.key).getOrElse {
    sys.error(s"Could not find organizations with key[${org.key}]")
  }

  lazy val teamId: Option[Long] = {
    form.teamKey.flatMap { key =>
      TeamsDao.lookupId(org, key)
    }
  }

  lazy val validate: Seq[Error] = {
    val keyErrors = form.teamKey match {
      case None => Seq.empty
      case Some(key) => {
        teamId match {
          case None => Seq(s"Team[$key] not found")
          case Some(_) => Seq.empty
        }
      }
    }

    val severityErrors = form.severity match {
      case Severity.UNDEFINED(value) => Seq(s"Invalid severity[$value]")
      case _ => Seq.empty
    }

    Validation.errors(keyErrors ++ severityErrors)
  }

}

object IncidentsDao {

  private val BaseQuery = s"""
    select incidents.id,
           organizations.key as organization_key, 
           organizations.name as organization_name,
           ${TeamsDao.select(Some("team"))},
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

  def findRecentlyModifiedIncidentIds(): Seq[Long] = {
    val query = BaseQuery + " and incidents.updated_at > now() - interval '3 days' "
    DB.withConnection { implicit c =>
      SQL(query)().toList.map { row =>
        row[Long]("id")
      }.toSeq
    }
  }

  def create(user: User, fullForm: FullIncidentForm): Incident = {
    val errors = fullForm.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))

    val id: Long = DB.withTransaction { implicit c =>
      val id = SQL(InsertQuery).on(
        'organization_id -> fullForm.orgId,
        'team_id -> fullForm.teamId,
        'severity -> fullForm.form.severity.toString,
        'summary -> fullForm.form.summary,
        'description -> fullForm.form.description,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))

      IncidentTagsDao.doUpdate(c, user, id, Seq.empty, fullForm.form.tags)

      id
    }

    global.Actors.mainActor ! actors.MeetingMessage.IncidentCreated(id)
    fullForm.teamId.map { _ =>
        global.Actors.mainActor ! actors.MeetingMessage.IncidentTeamUpdated(id)
    }

    findByOrganizationAndId(fullForm.org, id).getOrElse {
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
        'severity -> fullForm.form.severity.toString,
        'summary -> fullForm.form.summary,
        'description -> fullForm.form.description,
        'user_guid -> user.guid
      ).executeUpdate()

      IncidentTagsDao.doUpdate(c, user, incident.id, incident.tags, fullForm.form.tags)
    }

    global.Actors.mainActor ! actors.MeetingMessage.IncidentUpdated(incident.id)

    if (incident.team.map(_.key) != fullForm.form.teamKey) {
      fullForm.teamId.map { _ =>
        global.Actors.mainActor ! actors.MeetingMessage.IncidentTeamUpdated(incident.id)
      }
    }

    findByOrganizationAndId(fullForm.org, incident.id).getOrElse {
      sys.error("Failed to update incident")
    }
  }

  def softDelete(deletedBy: User, incident: Incident) {
    SoftDelete.delete("incidents", deletedBy, incident.id)
  }

  def findById(id: Long): Option[Incident] = {
    findAll(id = Some(id), limit = 1).headOption.map { i => findDetails(i) }
  }

  def findByOrganizationAndId(
    org: Organization,
    id: Long
  ): Option[Incident] = {
    findAll(org = Some(org), id = Some(id), limit = 1).headOption.map { i => findDetails(i) }
  }

  private def findDetails(incident: Incident): Incident = {
    incident.copy(tags = IncidentTagsDao.findAll(incidentId = Some(incident.id)).map(_.tag))
  }

  def findAll(
    org: Option[Organization] = None,
    id: Option[Long] = None,
    meetingId: Option[Long] = None,
    teamKey: Option[String] = None,
    hasTeam: Option[Boolean] = None,
    hasPlan: Option[Boolean] = None,
    hasGrade: Option[Boolean] = None,
    severity: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Incident] = {
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
      org.map { v => "and organizations.id = {organization_id}" },
      id.map { v => "and incidents.id = {id}" },
      meetingId.map { v => "and incidents.id in (select incident_id from agenda_items where deleted_at is null and meeting_id = {meeting_id})" },
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
      orgId.map { v => NamedParameter("organization_id", toParameterValue(v)) },
      id.map { v => NamedParameter("id", toParameterValue(v)) },
      meetingId.map { v => NamedParameter("meeting_id", toParameterValue(v)) },
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
          organization = OrganizationsDao.fromRow(row, Some("organization")),
          team = row[Option[String]]("team_key").map { _ => TeamsDao.fromRow(row, Some("team")) },
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
