package db

import com.gilt.quality.models.{Error, Organization, Team, TeamMember, TeamMemberSummary, User}
import lib.Validation
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import java.util.UUID

case class TeamMemberForm(
  org: Organization,
  teamKey: String,
  userGuid: UUID
) {

  lazy val teamId: Option[Long] = TeamsDao.lookupId(org, teamKey)

  lazy val validate: Seq[Error] = {
    val keyErrors = teamId match {
      case None => Seq(s"Team with key[${teamKey}] not found")
      case Some(team) => Seq.empty
    }

    val userErrors = UsersDao.findByGuid(userGuid) match {
      case None => Seq(s"User not found")
      case Some(team) => Seq.empty
    }

    Validation.errors(keyErrors ++ userErrors)
  }

}

object TeamMembersDao {

  private val BaseQuery = s"""
    select ${TeamsDao.select(Some("team"))},
           organizations.key as organization_key, 
           organizations.name as organization_name,
           users.guid as user_guid,
           users.email as user_email
      from team_members
      join teams on teams.deleted_at is null and teams.id = team_members.team_id
      join users on users.deleted_at is null and users.guid = team_members.user_guid
      join organizations on organizations.deleted_at is null and organizations.id = teams.organization_id
     where team_members.deleted_at is null
  """

  private val UpsertQuery = """
    insert into team_members
    (team_id, user_guid, created_by_guid)
    select {team_id}, {user_guid}::uuid, {created_by_guid}::uuid
     where not exists
      (select 1 from team_members where deleted_at is null and team_id = {team_id} and user_guid = {user_guid}::uuid)
  """

  private val RemoveQuery = """
    update team_members
       set deleted_at = now(), deleted_by_guid = {deleted_by_guid}::uuid
     where deleted_at is null
       and team_id = {team_id}
       and user_guid = {user_guid}::uuid
  """

  private val MemberSummaryQuery = s"""
    select count(*) as number_members
      from team_members
      join teams on teams.deleted_at is null and team_members.team_id = teams.id and teams.organization_id = {organization_id}
     where teams.key = {team_key}
       and team_members.deleted_at is null
  """

  def upsert(user: User, form: TeamMemberForm): TeamMember = {
    val errors = form.validate
    assert(errors.isEmpty, errors.map(_.message).mkString(" "))

    DB.withTransaction { implicit c =>
      val id = SQL(UpsertQuery).on(
        'team_id -> form.teamId,
        'user_guid -> form.userGuid,
        'created_by_guid -> user.guid
      ).execute()
    }

    findAll(
      form.org,
      teamKey = Some(form.teamKey),
      userGuid = Some(form.userGuid),
      limit = 1
    ).headOption.getOrElse {
      sys.error("Failed to create membership")
    }
  }

  def remove(user: User, form: TeamMemberForm) {
    DB.withTransaction { implicit c =>
      val id = SQL(RemoveQuery).on(
        'team_id -> form.teamId,
        'user_guid -> form.userGuid,
        'deleted_by_guid -> user.guid
      ).execute()
    }
  }

  def summary(
    org: Organization,
    team: Team
  ): TeamMemberSummary = {
    OrganizationsDao.lookupId(org.key) match {
      case None => TeamMemberSummary(team = team, numberMembers = 0)
      case Some(orgId) => {
        val bind = Seq(
          NamedParameter("organization_id", toParameterValue(orgId)),
          NamedParameter("team_key", toParameterValue(team.key))
        )

        DB.withConnection { implicit c =>
          SQL(MemberSummaryQuery).on(bind: _*)().toList.map { row =>
            TeamMemberSummary(
              team = team,
              numberMembers = row[Long]("number_members")
            )
          }.toSeq.head
        }
      }
    }
  }

  def findAll(
    org: Organization,
    teamKey: Option[String] = None,
    userGuid: Option[UUID] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[TeamMember] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      Some("and teams.organization_id = (select id from organizations where deleted_at is null and key = lower(trim({org_key})))"),
      teamKey.map { v => "and teams.key = lower(trim({team_key}))" },
      userGuid.map { v => "and team_members.user_guid = {user_guid}::uuid" },
      Some("order by teams.key, lower(users.email), team_members.id"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      Some(NamedParameter("org_key", toParameterValue(org.key))),
      teamKey.map { v => NamedParameter("team_key", toParameterValue(v)) },
      userGuid.map { v => NamedParameter("user_guid", toParameterValue(v)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { fromRow(_) }.toSeq
    }
  }

  private[db] def fromRow(
    row: anorm.Row
  ): TeamMember = {
    TeamMember(
      team = TeamsDao.fromRow(row, Some("team")),
      user = UsersDao.fromRow(row, Some("user"))
    )
  }

}
