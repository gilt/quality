package controllers

import com.gilt.quality.v0.models.{AuthenticationForm, User, UserForm}
import com.gilt.quality.v0.errors.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class UsersSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "POST /users" in new WithServer {
    val email = UUID.randomUUID.toString + "@gilttest.com"
    val user = createUser(UserForm(email = email))
    user.email must be(email)
  }

  "POST /users validates email is valid" in new WithServer {
    intercept[ErrorsResponse] {
      createUser(UserForm(email = "a"))
    }.errors.map(_.message) must be(Seq(s"email is not valid"))
  }

  "POST /users validates email does not already exist" in new WithServer {
    val email = UUID.randomUUID.toString + "@gilttest.com"
    val user = createUser(UserForm(email = email))
    intercept[ErrorsResponse] {
      createUser(UserForm(email=email))
    }.errors.map(_.message) must be(Seq(s"Email already exists"))
  }

  "GET /users by guid" in new WithServer {
    val user1 = createUser()
    val user2 = createUser()

    await(client.users.get(guid = Some(UUID.randomUUID))) must be(Seq.empty)
    await(client.users.get(guid = Some(user1.guid))).head must be(user1)
    await(client.users.get(guid = Some(user2.guid))).head must be(user2)
  }

  "GET /users by email" in new WithServer {
    val user1 = createUser()
    val user2 = createUser()

    await(client.users.get(email = Some(UUID.randomUUID.toString))) must be(Seq.empty)
    await(client.users.get(email = Some(user1.email))).head must be(user1)
    await(client.users.get(email = Some(user2.email))).head must be(user2)
  }

  "GET /users/:guid" in new WithServer {
    val user = createUser()
    await(client.users.getByGuid(user.guid)) must be(Some(user))
    await(client.users.getByGuid(UUID.randomUUID)) must be(None)
  }

  "POST /users/authenticate w/ invalid email" in new WithServer {
    intercept[ErrorsResponse] {
      await(client.users.postAuthenticate(AuthenticationForm(email = UUID.randomUUID.toString)))
    }.errors.map(_.message) must be(Seq(s"Email address not valid"))
  }

  "POST /users/authenticate w/ valid email" in new WithServer {
    val user = createUser()
    await(client.users.postAuthenticate(AuthenticationForm(email = user.email))) must be(user)
  }

}
