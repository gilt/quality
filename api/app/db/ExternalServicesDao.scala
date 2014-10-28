package db

import com.gilt.quality.models.{Error, ExternalService, ExternalServiceForm, ExternalServiceName, Organization, User}
import anorm._
import lib.{UrlKey, Validation}
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class FullExternalServiceForm(
  org: Organization,
  form: ExternalServiceForm
) {

  lazy val orgId = OrganizationsDao.lookupId(org.key).getOrElse {
    sys.error(s"Could not find organization with key[${org.key}]")
  }

  lazy val validate: Seq[Error] = {
    val nameErrors = form.name match {
      case ExternalServiceName.UNDEFINED(_) => Seq(s"Unrecognized external service[${form.name}]")
      case _ => Seq.empty
    }

    // TODO: Better URL validation
    val urlErrors = if (form.url.toLowerCase.startsWith("http")) {
      Seq.empty
    } else {
      Seq("URL must start with http")
    }

    Validation.errors(nameErrors ++ urlErrors)
  }

}

object ExternalServicesDao {

  private val BaseQuery = """
    select external_services.id,
           external_services.name,
           external_services.url,
           external_services.username,
           external_services.password
      from external_services
     where external_services.deleted_at is null
  """

  private val LookupPasswordQuery = """
    select password from external_services where deleted_at is null and id = {id}
  """

  def create(createdBy: User, fullForm: FullExternalServiceForm): ExternalService = {
    assert(fullForm.validate.isEmpty, fullForm.validate.map(_.message).mkString("\n"))

    val id = DB.withConnection { implicit c =>
      SQL("""
        insert into external_services
        (organization_id, name, url, username, password, created_by_guid)
        values
        ({organization_id}, {name}, {url}, {username}, {password}, {created_by_guid}::uuid)
      """).on(
        'organization_id -> fullForm.orgId,
        'name -> fullForm.form.name.toString,
        'url -> fullForm.form.url,
        'username -> fullForm.form.username,
        'password -> fullForm.form.password,
        'created_by_guid -> createdBy.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findByOrganizationAndId(fullForm.org, id).getOrElse {
      sys.error("Failed to create external service")
    }
  }

  def softDelete(deletedBy: User, service: ExternalService) {
    SoftDelete.delete("external_services", deletedBy, service.id)
  }

  def findByOrganizationAndId(
    org: Organization,
    id: Long
  ): Option[ExternalService] = {
    findAll(org, id = Some(id), limit = 1).headOption
  }

  private[db] def lookupPassword(service: ExternalService): String = {
    DB.withConnection { implicit c =>
      SQL(LookupPasswordQuery).on(
        'id -> service.id
      )().toList.map { row =>
        row[String]("password")
      }.toSeq.headOption.getOrElse {
        sys.error(s"Failed to lookup password for service id[${service.id}]")
      }
    }
  }

  def findAll(
    org: Organization,
    id: Option[Long] = None,
    name: Option[ExternalServiceName] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[ExternalService] = {
    OrganizationsDao.lookupId(org.key) match {
      case None => Seq.empty
      case Some(orgId) => {
        val sql = Seq(
          Some(BaseQuery.trim),
          Some("and external_services.organization_id = {organization_id}"),
          id.map { v => "and external_services.id = {id}" },
          name.map { v => "and external_services.name = {name}" },
          Some(s"order by lower(external_services.name) limit ${limit} offset ${offset}")
        ).flatten.mkString("\n   ")

        val bind = Seq[Option[NamedParameter]](
          Some('organization_id -> orgId),
          id.map('id -> _),
          name.map('name -> _.toString)
        ).flatten

        DB.withConnection { implicit c =>
          SQL(sql).on(bind: _*)().toList.map { fromRow(_) }.toSeq
        }
      }
    }
  }

  private[db] def fromRow(
    row: anorm.Row,
    prefix: Option[String] = None
  ): ExternalService = {
    val p = prefix.map( _ + "_").getOrElse("")
    ExternalService(
      id = row[Long](s"${p}id"),
      name = ExternalServiceName(row[String](s"${p}name")),
      url = row[String](s"${p}url"),
      username = row[String](s"${p}username")
    )
  }

}
