package db

import com.gilt.quality.models.{Error, Subscription, SubscriptionForm, User}
import anorm._
import lib.Validation
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import java.util.UUID

object SubscriptionsDao {

  private val BaseQuery = """
    select subscriptions.id,
           publications.key as publication_key,
           users.guid as user_guid,
           users.email as user_email
      from subscriptions
      join publications on publications.id = subscriptions.publication_id and publications.deleted_at is null
      join users on users.id = publications.user_id and users.deleted_at is null
     where subscriptions.deleted_at is null
  """

  private val SoftDeleteQuery = """
    update subscriptions
       set deleted_by_guid = {deleted_by_guid}::uuid, deleted_at = now()
     where deleted_at is null
       and user_guid = {user_guid}::uuid
       and publication_id = (select id from publications where deleted_at is null and key = {publication_key})
  """

  def validate(
    form: SubscriptionForm
  ): Seq[Error] = {
    val organizationKeyErrors = OrganizationsDao.lookupId(form.organizationKey) match {
        case None => Seq("Organization not found")
        case Some(_) => Seq.empty
    }

    val publicationKeyErrors = PublicationsDao.lookupId(form.publicationKey) match {
        case None => Seq("Publication not found")
        case Some(_) => Seq.empty
    }

    val userErrors = UsersDao.findByGuid(form.userGuid) match {
        case None => Seq("User not found")
        case Some(_) => Seq.empty
    }

    Validation.errors(organizationKeyErrors ++ publicationKeyErrors ++ userErrors)
  }

  def create(createdBy: User, form: SubscriptionForm): Subscription = {
    val errors = validate(form)
    assert(errors.isEmpty, errors.map(_.message).mkString("\n"))

    val organizationId = OrganizationsDao.lookupId(form.organizationKey).get
    val publicationId = PublicationsDao.lookupId(form.publicationKey).get

    val id = DB.withConnection { implicit c =>
      SQL("""
        insert into subscriptions
        (organization_id, publication_id, user_guid, created_by_guid)
        values
        ({organization_id}, {publication_id}, {user_guid}::uuid, {created_by_guid}::uuid)
      """).on(
        'organization_id -> organizationId,
        'publication_id -> publicationId,
        'user_guid -> form.userGuid,
        'created_by_guid -> createdBy.guid
      ).executeInsert().getOrElse(sys.error("Missing id"))
    }

    findById(id).getOrElse {
      sys.error("Failed to create subscription")
    }
  }

  def softDelete(deletedBy: User, subscription: Subscription) {
    DB.withConnection { implicit c =>
      SQL(SoftDeleteQuery).on(
        'deleted_by_guid -> deletedBy.guid,
        'publication_id -> subscription.publication.key,
        'user_guid -> subscription.user.guid
      ).execute()
    }
  }

  def findById(id: Long): Option[Subscription] = {
    findAll(id = Some(id), limit = 1).headOption
  }

  def findAll(
    id: Option[Long] = None,
    organizationKey: Option[String] = None,
    userGuid: Option[UUID] = None,
    publicationKey: Option[String] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Subscription] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      id.map { v => "and subscriptions.id = {id}" },
      organizationKey.map { v => "and subscriptions.organization_id = (select id from organizations where deleted_at is null and key = {organization_key})" },
      userGuid.map { v => "and subscriptions.user_guid = {user_guid}::uuid" },
      publicationKey.map { v => "and subscriptions.publication_id = (select id from publications where deleted_at is null and key = {publication_key})" },
      Some(s"order by lower(subscriptions.name) limit ${limit} offset ${offset}")
    ).flatten.mkString("\n   ")

    val bind = Seq[Option[NamedParameter]](
      id.map('id -> _),
      organizationKey.map('organization_key -> _),
      userGuid.map('user_guid -> _.toString),
      publicationKey.map('publication_key -> _)
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { fromRow(_) }.toSeq
    }
  }

  private[db] def fromRow(
    row: anorm.Row
  ): Subscription = {
    Subscription(
      id = row[Long]("id"),
      user = UsersDao.fromRow(row, Some("user")),
      publication = PublicationsDao.fromRow(row, Some("publication"))
    )
  }

}
