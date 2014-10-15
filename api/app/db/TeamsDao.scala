package db

import com.gilt.quality.models.{Error, Icons, Organization, Team, TeamForm}
import lib.{UrlKey, Validation}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class FullTeamForm(
  org: Organization,
  form: TeamForm
) {

  lazy val orgId = OrganizationsDao.lookupId(org.key).getOrElse {
    sys.error(s"Could not find organizations with key[${org.key}]")
  }

  lazy val validate: Seq[Error] = {
    val keyErrors = TeamsDao.findByKey(org, form.key) match {
      case Some(team) => {
        Seq(s"Team with key[${form.key}] already exists")
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

  private val LookupIdQuery = """
    select teams.id
      from teams
      left join organizations on organizations.id = teams.organization_id and organizations.key = {org_key}
     where teams.deleted_at is null
       and teams.key = {key}
  """

  def create(user: User, fullForm: FullTeamForm): Team = {
    val errors = fullForm.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))

    val id: Long = DB.withTransaction { implicit c =>
      val id = SQL(InsertQuery).on(
        'organization_id -> fullForm.orgId,
        'key -> fullForm.form.key.trim.toLowerCase,
        'email -> fullForm.form.email.map(_.trim),
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))

      fullForm.form.icons.foreach { icons =>
        TeamIconsDao.create(c, user, id, icons)
      }

      id
    }

    findByKey(fullForm.org, fullForm.form.key).getOrElse {
      sys.error("Failed to create team")
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
    org: Organization,
    key: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Team] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      Some("and teams.organization_id = (select id from organizations where deleted_at is null and key = {org_key})"),
      key.map { v => "and teams.key = {key}" },
      Some("order by teams.key"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      Some(NamedParameter("org_key", toParameterValue(org.key))),
      key.map { v => NamedParameter("key", toParameterValue(v.trim.toLowerCase)) }
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
      organization = OrganizationsDao.fromRow(row, Some("organization"))
    )
  }

}
