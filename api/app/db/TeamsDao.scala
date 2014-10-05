package db

import com.gilt.quality.models.{Error, Organization, Team, TeamForm}
import lib.Validation
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class FullTeamForm(
  org: Organization,
  form: TeamForm
) {

  lazy val validate: Seq[Error] = {
    TeamsDao.findByKey(org, form.key) match {
      case None => Seq.empty
      case Some(key) => {
        Validation.error(s"Team with key[$key] already exists")
      }
    }
  }

}

object TeamsDao {

  private val BaseQuery = """
    select teams.key,
           organizations.key as organization_key, 
           organizations.name as organization_name
      from teams
      join organizations on organizations.deleted_at is null and organizations.id = teams.organization_id
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into teams
    (organization_id, key, created_by_guid, updated_by_guid)
    values
    ((select id from organizations where deleted_at is null and key = '{organization_key}'), {key}, {user_guid}::uuid, {user_guid}::uuid)
  """

  private val SoftDeleteQuery = """
    update teams set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now() where key = {key} and deleted_at is null
  """

  private val LookupIdQuery = """
    select teams.id
      from teams
      left join organizations on organizations.id = teams.organization_id and organizations.key = {org_key}
     where teams.deleted_at is null
       and teams.key = {key} and key = {key}
  """

  def create(user: User, fullForm: FullTeamForm): Team = {
    assert(fullForm.validate.isEmpty, fullForm.validate.map(_.message).mkString(" "))

    val id: Long = DB.withTransaction { implicit c =>
      SQL(InsertQuery).on(
        'organization_key -> fullForm.org.key,
        'key -> fullForm.form.key.trim.toLowerCase,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findByKey(fullForm.org, fullForm.form.key).getOrElse {
      sys.error("Failed to create team")
    }
  }

  def softDelete(deletedBy: User, team: Team) {
    DB.withConnection { implicit c =>
      SQL(SoftDeleteQuery).on('deleted_by_guid -> deletedBy.guid, 'key -> team.key).execute()
    }
  }

  def findByKey(
    org: Organization,
    key: String
  ): Option[Team] = {
    findAll(orgKey = org.key, key = Some(key), limit = 1).headOption
  }

  def lookupId(
    org: Organization,
    key: String
  ): Option[Long] = {
    DB.withConnection { implicit c =>
      SQL(LookupIdQuery).on(
        'org_key -> org.key,
        'key -> key.trim.toLowerCase
      )().toList.map { row =>
        row[Long]("id")
      }.toSeq.headOption
    }
  }

  def findAll(
    orgKey: String,
    key: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Team] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      Some(" and teams.organization_id = (select id from organizations where deleted_at is null and key = {org_key}) "),
      key.map { v => "and teams.key = {key}" },
      Some("order by teams.key"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      Some(NamedParameter("org_key", toParameterValue(orgKey))),
      key.map { v => NamedParameter("key", toParameterValue(v.trim.toLowerCase)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Team(
          key = row[String]("key"),
          organization = Organization(
            key = row[String]("organization_key"),
            name = row[String]("organization_name")
          )
        )
      }.toSeq
    }
  }

}
