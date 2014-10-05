package db

import com.gilt.quality.models.Organization
import anorm._
import lib.{UrlKey, Validation, ValidationError}
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import scala.annotation.tailrec

case class OrganizationForm(
  name: String,
  key: Option[String] = None
)

object OrganizationForm {
  implicit val organizationFormReads = Json.reads[OrganizationForm]
}

object OrganizationsDao {

  private val MinNameLength = 4

  private val BaseQuery = """
    select organizations.id, organizations.name, organizations.key
      from organizations
     where organizations.deleted_at is null
  """

  def validate(
    form: OrganizationForm
  ): Seq[ValidationError] = {
    val nameErrors = if (form.name.length < MinNameLength) {
      Seq(s"name must be at least $MinNameLength characters")
    } else {
      form.key match {
        case None => Seq.empty
        case Some(key: String) => {
          findAll(key = Some(key), limit = 1).headOption match {
            case None => Seq.empty
            case Some(org: Organization) => {
              Seq("Org with this key already exists")
            }
          }
        }
      }
    }

    val keyErrors = form.key match {
      case None => Seq.empty
      case Some(key) => {
        val generated = UrlKey.generate(key)
        if (key == generated) {
          Seq.empty
        } else {
          Seq(s"Key must be in all lower case and contain alphanumerics only. A valid key would be: $generated")
        }
      }
    }

    Validation.errors(nameErrors ++ keyErrors)
  }

  def create(createdBy: User, form: OrganizationForm): Organization = {
    val errors = validate(form)
    require(errors.isEmpty, errors.map(_.message).mkString("\n"))

    val key = form.key.getOrElse(generateKey(form.name))

    val id = DB.withConnection { implicit c =>
      SQL("""
        insert into organizations
        (name, key, created_by_guid, updated_by_guid)
        values
        ({name}, {key}, {created_by_guid}::uuid, {updated_by_guid}::uuid)
      """).on(
        'name -> form.name.trim,
        'key -> key,
        'created_by_guid -> createdBy.guid,
        'updated_by_guid -> createdBy.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create org")
    }
  }

  def softDelete(deletedBy: User, org: Organization) {
    SoftDelete.deleteByKey("organizations", deletedBy, org.key)
  }

  def findById(id: Long): Option[Organization] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findByKey(key: String): Option[Organization] = {
    findAll(key = Some(key), limit = 1).headOption
  }

  def findAll(
    id: Option[Long] = None,
    key: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Organization] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and organizations.id = {id}" },
      key.map { v => "and organizations.key = lower(trim({key}))" },
      Some(s"order by lower(organizations.name) limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq[Option[NamedParameter]](
      id.map('id -> _),
      key.map('key -> _)
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Organization(
          name = row[String]("name"),
          key = row[String]("key")
        )
      }.toSeq
    }
  }

  @tailrec
  private def generateKey(base: String, iteration: Int = 1): String = {
    val prefix = UrlKey.generate(base)
    val key = if (iteration == 1) { UrlKey.generate(base) } else { UrlKey.generate(base) + "-" + iteration }
    findAll(key = Some(key), limit = 1).headOption match {
      case None => key
      case Some(org) => generateKey(base, iteration + 1)
    }
  }

}
