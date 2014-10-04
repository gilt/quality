package db

import anorm._
import play.api.db._
import play.api.Play.current

private[db] object SoftDelete {

  private val SoftDeleteQuery = """
    update %s set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now() where id = {id} and deleted_at is null
  """

  def delete(tableName: String, deletedBy: User, id: Long) {
    DB.withConnection { implicit c =>
      delete(c, tableName, deletedBy, id)
    }
  }

  def delete(implicit c: java.sql.Connection, tableName: String, deletedBy: User, id: Long) {
    SQL(SoftDeleteQuery.format(tableName)).on('deleted_by_guid -> deletedBy.guid, 'id -> id).execute()
  }

}
