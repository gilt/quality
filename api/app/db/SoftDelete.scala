package db

import com.gilt.quality.v0.models.User
import anorm._
import play.api.db._
import play.api.Play.current
import java.util.UUID

private[db] object SoftDelete {

  private val SoftDeleteQuery = """
    update %s set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now() where id = {id} and deleted_at is null
  """

  private val SoftDeleteByKeyQuery = """
    update %s set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now() where key = {key} and deleted_at is null
  """

  private val SoftDeleteByGuidQuery = """
    update %s set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now(), updated_at = now() where guid = {guid}::uuid and deleted_at is null
  """

  def delete(tableName: String, deletedBy: User, id: Long) {
    DB.withConnection { implicit c =>
      delete(c, tableName, deletedBy, id)
    }
  }

  def delete(implicit c: java.sql.Connection, tableName: String, deletedBy: User, id: Long) {
    SQL(SoftDeleteQuery.format(tableName)).on('deleted_by_guid -> deletedBy.guid, 'id -> id).execute()
  }


  def deleteByKey(tableName: String, deletedBy: User, key: String) {
    DB.withConnection { implicit c =>
      deleteByKey(c, tableName, deletedBy, key)
    }
  }

  def deleteByKey(implicit c: java.sql.Connection, tableName: String, deletedBy: User, key: String) {
    SQL(SoftDeleteByKeyQuery.format(tableName)).on('deleted_by_guid -> deletedBy.guid, 'key -> key).execute()
  }

  def deleteByGuid(tableName: String, deletedBy: User, guid: UUID) {
    DB.withConnection { implicit c =>
      SQL(SoftDeleteByGuidQuery.format(tableName)).on('deleted_by_guid -> deletedBy.guid, 'guid -> guid.toString).execute()
    }
  }

}
