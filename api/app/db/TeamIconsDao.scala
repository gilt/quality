package db

import com.gilt.quality.models.{Icons, Team, User}
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

  private val DeleteByTeamIdQuery = """
    update team_icons
       set deleted_by_guid = {deleted_by_guid}::uuid,
           deleted_at = now(), updated_at = now()
     where team_id = {team_id}
       and deleted_at is null
  """

  def create(
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
    teamId: Long
  ) {
    SQL(DeleteByTeamIdQuery).on('deleted_by_guid -> deletedBy.guid, 'team_id -> teamId).execute()
  }

}
