
package controllers

import com.gilt.quality.models.{Organization, OrganizationForm}
import com.gilt.quality.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class OrganizationsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "POST /organizations will default key to name" in new WithServer {
    val guid = UUID.randomUUID.toString
    val org = createOrganization(OrganizationForm(name = guid))
    org.name must be(guid)
    org.key must be(guid)
  }

  "POST /organizations allows setting key" in new WithServer {
    val name = UUID.randomUUID.toString
    val key = UUID.randomUUID.toString
    val org = createOrganization(OrganizationForm(name = name, key = Some(key)))
    org.name must be(name)
    org.key must be(key)
  }

  "POST /organizations validates name is valid" in new WithServer {
    intercept[ErrorsResponse] {
      createOrganization(OrganizationForm(name = "a"))
    }.errors.map(_.message) must be(Seq(s"name must be at least ${db.OrganizationsDao.MinNameLength} characters"))
  }

  "POST /organizations validates key is not reserved" in new WithServer {
    intercept[ErrorsResponse] {
      createOrganization(OrganizationForm(name = UUID.randomUUID.toString, key = Some("user")))
    }.errors.map(_.message) must be(Seq(s"Key user is a reserved word and cannot be used for the key of an organization"))
  }

  "POST /organizations validates key is valid" in new WithServer {
    val name = UUID.randomUUID.toString

    intercept[ErrorsResponse] {
      createOrganization(OrganizationForm(name = name, key = Some("a")))
    }.errors.map(_.message) must be(Seq(s"Key must be at least ${db.OrganizationsDao.MinKeyLength} characters"))

    intercept[ErrorsResponse] {
      createOrganization(OrganizationForm(name = name, key = Some("a bad key")))
    }.errors.map(_.message) must be(Seq(s"Key must be in all lower case and contain alphanumerics only. A valid key would be: a-bad-key"))
  }

  "DELETE /organizations/:key" in new WithServer {
    val org = createOrganization()
    await(client.organizations.deleteByKey(org.key)) must be(Some(()))
    await(client.organizations.get(key = Some(org.key))) must be(Seq.empty)
  }

  "GET /organizations" in new WithServer {
    val org1 = createOrganization()
    val org2 = createOrganization()

    await(client.organizations.get(key = Some(UUID.randomUUID.toString))) must be(Seq.empty)
    await(client.organizations.get(key = Some(org1.key))).head must be(org1)
    await(client.organizations.get(key = Some(org2.key))).head must be(org2)
  }

  "GET /organizations/:key" in new WithServer {
    val org = createOrganization()
    await(client.organizations.getByKey(org.key)) must be(Some(org))
    await(client.organizations.getByKey(UUID.randomUUID.toString)) must be(None)
  }

}
