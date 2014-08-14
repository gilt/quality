package db

import com.gilt.quality.models.Team
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class TeamForm(
  key: String
)

object TeamForm {
  implicit val readsTeamForm = Json.reads[TeamForm]
}

object TeamsDao {

  private val BaseQuery = """
    select key
      from teams
     where deleted_at is null
  """

  private val InsertQuery = """
    insert into teams
    (key, created_by_guid, updated_by_guid)
    values
    ({key}, {user_guid}::uuid, {user_guid}::uuid)
  """

  private val SoftDeleteQuery = """
    update teams set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now() where key = {key} and deleted_at is null
  """

  private val LookupIdQuery = """
    select id from teams where deleted_at is null and key = {key}
  """

  def create(user: User, form: TeamForm): Team = {
    val id: Long = DB.withTransaction { implicit c =>
      SQL(InsertQuery).on(
        'key -> form.key.trim.toLowerCase,
        'user_guid -> user.guid,
        'user_guid -> user.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findByKey(form.key).getOrElse {
      sys.error("Failed to create team")
    }
  }

  def softDelete(deletedBy: User, team: Team) {
    DB.withConnection { implicit c =>
      SQL(SoftDeleteQuery).on('deleted_by_guid -> deletedBy.guid, 'key -> team.key).execute()
    }
  }

  def findByKey(key: String): Option[Team] = {
    findAll(key = Some(key.trim.toLowerCase), limit = 1).headOption
  }

  def lookupId(key: String): Option[Long] = {
    DB.withConnection { implicit c =>
      SQL(LookupIdQuery).on('key -> key.trim.toLowerCase)().toList.map { row =>
        row[Long]("id")
      }.toSeq.headOption
    }
  }

  def findAll(key: Option[String] = None,
              limit: Int = 50,
              offset: Int = 0): Seq[Team] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      key.map { v => "and teams.key = {key}" },
      Some("order by teams.key"),
      Some(s"limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      key.map { v => NamedParameter("key", toParameterValue(v.trim.toLowerCase)) }
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Team(
          key = row[String]("key")
        )
      }.toSeq
    }
  }

}
