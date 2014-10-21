package db

import com.gilt.quality.models.Publication
import anorm._
import play.api.db._
import play.api.Play.current

object PublicationsDao {

  private val BaseQuery = """
    select publications.key
      from publications
     where publications.deleted_at is null
  """

  private val LookupIdQuery = """
    select id from publications where deleted_at is null and key = lower(trim({key}))
  """

  def findByKey(key: String): Option[Publication] = {
    findAll(
      key = Some(key),
      limit = 1
    ).headOption
  }

  private[db] def lookupId(
    key: String
  ): Option[Long] = {
    DB.withConnection { implicit c =>
      SQL(LookupIdQuery).on(
        'key -> key
      )().toList.map { row =>
        row[Long]("id")
      }.toSeq.headOption
    }
  }

  def findAll(
    key: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Publication] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      key.map { v => "and publications.key = lower(trim({key}))" },
      Some(s"order by lower(organizations.name) limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq[Option[NamedParameter]](
      key.map('key -> _)
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { fromRow(_) }.toSeq
    }
  }

  private[db] def fromRow(
    row: anorm.Row,
    prefix: Option[String] = None
  ): Publication = {
    val p = prefix.map( _ + "_").getOrElse("")
    Publication(
      key = row[String](s"${p}key")
    )
  }

}
