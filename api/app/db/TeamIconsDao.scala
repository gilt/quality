package db

import com.gilt.quality.models.{Icons, Team}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

private[db] case class TeamIcon(name: String, url: String)

private[db] object TeamIconsDao {

  val Smiley = "smiley"
  val Frowny = "frowny"

  private val InsertQuery = """
    insert into team_icons
    (team_id, name, url, created_by_guid)
    values
    ({team_id}, {name}, {url}, {user_guid}::uuid)
  """

  private val DeleteByTeamKeyQuery = """
    update team_icons
       set deleted_by_guid = {deleted_by_guid}::uuid,
           deleted_at = now(), updated_at = now()
     where team_id = (select id from teams where deleted_at is null and key = {team_key})
       and deleted_at is null
  """

  def create(
    implicit conn: java.sql.Connection,
    user: User, 
    teamId: Long,
    icons: Icons
  ) {

    if (icons.smileyUrl != Defaults.Icons.smileyUrl) {
      store(conn, user, teamId, TeamIcon(Smiley, icons.smileyUrl))
    }

    if (icons.frownyUrl != Defaults.Icons.frownyUrl) {
      store(conn, user, teamId, TeamIcon(Frowny, icons.frownyUrl))
    }

  }

  private[this] def store(
    implicit conn: java.sql.Connection,
    user: User, 
    teamId: Long,
    teamIcon: TeamIcon
  ) {
    SQL(InsertQuery).on(
      'team_id -> teamId,
      'name -> teamIcon.name,
      'url -> teamIcon.url,
      'user_guid -> user.guid
    ).execute()
  }

  def softDelete(
    implicit conn: java.sql.Connection,
    deletedBy: User,
    team: Team
  ) {
    SQL(DeleteByTeamKeyQuery).on('deleted_by_guid -> deletedBy.guid, 'team_key -> team.key).execute()
  }

}
