package db

import core.Defaults
import com.gilt.quality.v0.models.{Error, Icons, Organization, Team, TeamForm, User}
import lib.{UrlKey, Validation}
import anorm._
import anorm.ParameterValue._
import java.util.UUID
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class FullTeamForm(
  org: Organization,
  form: TeamForm,
  existing: Option[Team] = None
) {

  lazy val orgId = OrganizationsDao.lookupId(org.key).getOrElse {
    sys.error(s"Could not find organization with key[${org.key}]")
  }

  lazy val validate: Seq[Error] = {
    val keyErrors = TeamsDao.findByKey(org, form.key) match {
      case Some(team) => {
        existing match {
          case None => Seq(s"Team with key[${form.key}] already exists")
          case Some(e) => {
            if (e.key == form.key) {
              Nil
            } else {
              Seq(s"Team with key[${form.key}] already exists")
            }
          }
        }
      }
      case None => {
        val generated = UrlKey.generate(form.key)
        if (form.key == generated) {
          Seq.empty
        } else {
          Seq(s"Key must be in all lower case and contain alphanumerics only. A valid key would be: $generated")
        }
      }
    }

    val emailErrors: Seq[String] = form.email.map { email =>
      if (email.indexOf("@") <= 0) {
        Seq("Email address is not valid")
      } else {
        Seq.empty
      }
    }.getOrElse(Seq.empty)

    Validation.errors(keyErrors ++ emailErrors)
  }

}

object TeamsDao {

  def select(prefix: Option[String] = None): String = {
    val p = prefix.map( _ + "_").getOrElse("")
    s"""
      teams.key as ${p}key, teams.email as ${p}email,
      array_to_json(array(select row_to_json(team_icons)
                            from team_icons
                           where team_icons.team_id = teams.id
                             and team_icons.deleted_at is null))::varchar as ${p}icons
    """.trim
  }

  private val BaseQuery = s"""
    select ${select()},
           organizations.key as organization_key, 
           organizations.name as organization_name
      from teams
      join organizations on organizations.deleted_at is null and organizations.id = teams.organization_id
     where teams.deleted_at is null
  """

  private val InsertQuery = """
    insert into teams
    (organization_id, key, email, created_by_guid, updated_by_guid)
    values
    ({organization_id}, {key}, {email}, {user_guid}::uuid, {user_guid}::uuid)
  """

  private val UpdateQuery = """
    update teams
       set key = {key},
           email = {email},
           updated_by_guid = {user_guid}::uuid
     where id = {id}
  """

  private val LookupIdQuery = """
    select teams.id
      from teams
     where teams.deleted_at is null
       and teams.key = {key}
       and teams.organization_id = {organization_id}
  """

  def create(user: User, fullForm: FullTeamForm): Team = {
    val errors = fullForm.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))

    val id: Long = DB.withTransaction { implicit c =>
      val id = SQL(InsertQuery).on(
        'organization_id -> fullForm.orgId,
        'key -> UrlKey.generate(fullForm.form.key),
        'email -> fullForm.form.email.map(_.trim),
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))

      updateIcons(c, user, id, fullForm.form)

      id
    }

    findByKey(fullForm.org, fullForm.form.key).getOrElse {
      sys.error("Failed to create team")
    }
  }

  def update(user: User, team: Team, fullForm: FullTeamForm): Team = {
    val errors = fullForm.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))

    val id = lookupId(team.organization, team.key).getOrElse {
      sys.error(s"Could not find team[${team.organization.key}/${team.key}] to update")
    }

    val key = UrlKey.generate(fullForm.form.key)

    DB.withTransaction { implicit c =>
      SQL(UpdateQuery).on(
        'key -> key,
        'email -> fullForm.form.email.map(_.trim),
        'user_guid -> user.guid,
        'id -> id
      ).execute()

      TeamIconsDao.softDelete(c, user, id)
      updateIcons(c, user, id, fullForm.form)
    }

    findByKey(team.organization, key).getOrElse {
      sys.error("Failed to update team")
    }
  }

  private[this] def updateIcons(
    implicit conn: java.sql.Connection,
    user: User,
    teamId: Long,
    form: TeamForm
  ) {
    form.smileyUrl.foreach { url =>
      if (url != Defaults.Icons.smileyUrl) {
        TeamIconsDao.create(conn, user, teamId, TeamIcon(TeamIconsDao.Smiley, url))
      }
    }

    form.frownyUrl.foreach { url =>
      if (url != Defaults.Icons.frownyUrl) {
        TeamIconsDao.create(conn, user, teamId, TeamIcon(TeamIconsDao.Frowny, url))
      }
    }
  }

  def softDelete(deletedBy: User, team: Team) {
    SoftDelete.deleteByKey("teams", deletedBy, team.key)
  }

  def findByKey(
    org: Organization,
    key: String
  ): Option[Team] = {
    findAll(org, key = Some(key), limit = 1).headOption
  }

  def lookupId(
    org: Organization,
    key: String
  ): Option[Long] = {
    OrganizationsDao.lookupId(org.key).flatMap { orgId =>
      DB.withConnection { implicit c =>
        SQL(LookupIdQuery).on(
          'organization_id -> orgId,
          'key -> key.trim.toLowerCase
        )().toList.map { row =>
          row[Long]("id")
        }.toSeq.headOption
      }
    }
  }

  def findAll(
    org: Organization,
    key: Option[String] = None,
    userGuid: Option[UUID] = None,
    excludeUserGuid: Option[UUID] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Team] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      Some("and teams.organization_id = (select id from organizations where deleted_at is null and key = {org_key})"),
      key.map { v => "and teams.key = lower(trim({key}))" },
      userGuid.map { v => "and teams.id in (select team_id from team_members where deleted_at is null and user_guid = {user_guid}::uuid)" },
      excludeUserGuid.map { v => "and teams.id not in (select team_id from team_members where deleted_at is null and user_guid = {exclude_user_guid}::uuid)" },
      Some("order by teams.key"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      Some(NamedParameter("org_key", toParameterValue(org.key))),
      key.map { v => NamedParameter("key", toParameterValue(v)) },
      userGuid.map { v => NamedParameter("user_guid", toParameterValue(v.toString)) },
      excludeUserGuid.map { v => NamedParameter("exclude_user_guid", toParameterValue(v.toString)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { fromRow(_) }.toSeq
    }
  }

  private[db] def fromRow(
    row: anorm.Row,
    prefix: Option[String] = None,
    organizationPrefix: String = "organization"
  ): Team = {
    val p = prefix.map( _ + "_").getOrElse("")
    val icons = Json.parse(row[String](s"${p}icons")).as[JsArray].value.map(_.as[JsObject]).map { json =>
      TeamIcon(
        name = (json \ "name").as[String],
        url = (json \ "url").as[String]
      )
    }

    Team(
      key = row[String](s"${p}key"),
      email = row[Option[String]](s"${p}email"),
      icons = Icons(
        smileyUrl = icons.find(_.name == TeamIconsDao.Smiley).map(_.url).getOrElse(Defaults.Icons.smileyUrl),
        frownyUrl = icons.find(_.name == TeamIconsDao.Frowny).map(_.url).getOrElse(Defaults.Icons.frownyUrl)
      ),
      organization = OrganizationsDao.fromRow(row, Some(organizationPrefix))
    )
  }

}
