package db

import com.gilt.quality.models.{AuthenticationForm, Error, User, UserForm}
import anorm._
import lib.Validation
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import java.util.UUID

object UsersDao {

  /**
    * Represents changes created by the background actors
    */
  val Actor = com.gilt.quality.models.User(
    guid = UUID.fromString("9472ae70-30c2-012c-8f71-0015177442e6"),
    email = "otto@gilt.com"
  )

  /**
    * TODO: Remove this and replace with actual user guid once we ask
    * people to login
    */
  val Default = Actor

  val MinNameLength = 4
  val MinKeyLength = 4

  private val BaseQuery = """
    select users.guid, users.email
      from users
     where users.deleted_at is null
  """

  def validate(
    form: UserForm
  ): Seq[Error] = {
    val index = form.email.indexOf("@")
    val emailErrors = if (index <= 0) {
      Seq(s"email is not valid")
    } else {
      findByEmail(form.email) match {
        case None => Seq.empty
        case Some(_) => Seq("Email already exists")
      }
    }

    Validation.errors(emailErrors)
  }

  def create(createdBy: User, form: UserForm): User = {
    val errors = validate(form)
    assert(errors.isEmpty, errors.map(_.message).mkString("\n"))

    val guid = UUID.randomUUID

    DB.withConnection { implicit c =>
      SQL("""
        insert into users
        (guid, email, created_by_guid, updated_by_guid)
        values
        ({guid}::uuid, {email}, {user_guid}::uuid, {user_guid}::uuid)
      """).on(
        'guid -> guid,
        'email -> form.email,
        'user_guid -> createdBy.guid
      ).execute()
    }

    findByGuid(guid).getOrElse {
      sys.error("Failed to create user")
    }
  }

  def findByGuid(guid: UUID): Option[User] = findOne(guid = Some(guid))

  def findByEmail(email: String): Option[User] = findOne(email = Some(email))

  def findOne(
    guid: Option[UUID] = None,
    email: Option[String] = None
  ): Option[User] = {
    assert(!guid.isEmpty || !email.isEmpty, "guid or email is required")

    val sql = Seq(
      Some(BaseQuery.trim),
      guid.map { v => "and users.guid = {guid}::uuid" },
      email.map { v => "and users.email = lower(trim({email}))" },
      Some("limit 1")
    ).flatten.mkString("\n   ")

    val bind = Seq[Option[NamedParameter]](
      guid.map('guid -> _.toString),
      email.map('email -> _)
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { fromRow(_) }.toSeq
    }.headOption
  }

  private[db] def fromRow(
    row: anorm.Row,
    prefix: Option[String] = None
  ): User = {
    val p = prefix.map( _ + "_").getOrElse("")
    User(
      guid = row[UUID](s"${p}guid"),
      email = row[String](s"${p}email")
    )
  }

}
