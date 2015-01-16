package controllers

import db.ExternalServicesDao
import com.gilt.quality.v0.error.FailedRequest
import com.gilt.quality.v0.models.{ExternalService, ExternalServiceForm, ExternalServiceName, Organization}
import com.gilt.quality.v0.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class ExternalServicesSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  def createExternalService(
    org: Organization = createOrganization(),
    name: ExternalServiceName = ExternalServiceName.Jira,
    url: String = "http://localhost"
  ): ExternalService = {
    await(
      client.externalServices.postByOrg(
        org = org.key,
        externalServiceForm = ExternalServiceForm(
          name = name,
          url = url,
          username = "test",
          password = "password"
        )
      )
    )
  }

  "POST /:org/external_services" in new WithServer {
    val key = UUID.randomUUID.toString
    val service = createExternalService(name = ExternalServiceName.Jira)
    service.name must be(ExternalServiceName.Jira)
    service.url must be("http://localhost")
    service.username must be("test")
    ExternalServicesDao.lookupPassword(service) must be("password")
  }

  "POST /:org/external_services validates name" in new WithServer {
    val key = UUID.randomUUID.toString
    intercept[ErrorsResponse] {
      createExternalService(name = ExternalServiceName("foo"))
    }.errors.map(_.message) must be(Seq("Unrecognized external service[foo]"))
  }

  "POST /:org/external_services validates url" in new WithServer {
    val key = UUID.randomUUID.toString
    intercept[ErrorsResponse] {
      createExternalService(url = "foo")
    }.errors.map(_.message) must be(Seq("URL must start with http"))
  }

  "POST /:org/external_services validates that name is unique" in new WithServer {
    val service = createExternalService()
    intercept[ErrorsResponse] {
      createExternalService(org = service.organization, name = service.name)
    }.errors.map(_.message) must be(Seq(s"External service with name[${service.name}] already exists"))
  }

  "GET /:org/external_services/:id" in new WithServer {
    val service = createExternalService()
    await(client.externalServices.getByOrgAndId(service.organization.key, service.id)).map(_.id) must be(Some(service.id))
  }

  "DELETE /:org/external_services/:id" in new WithServer {
    val service = createExternalService()
    await(client.externalServices.deleteByOrgAndId(service.organization.key, service.id)) must be(Some(()))
    await(client.externalServices.getByOrgAndId(service.organization.key, service.id)) must be(None)
  }

  "GET /:org/external_services by id" in new WithServer {
    val org = createOrganization()
    val service1 = createExternalService(org)

    await(client.externalServices.getByOrg(org.key, id = Some(0))) must be(Seq.empty)
    await(client.externalServices.getByOrg(org.key, id = Some(service1.id))).head must be(service1)
  }

  "GET /:org/external_services by name" in new WithServer {
    val org = createOrganization()
    val service = createExternalService(org)

    await(client.externalServices.getByOrg(org.key, name = Some(service.name))) must be(Seq(service))

    intercept[FailedRequest] {
      await(client.externalServices.getByOrg(org.key, name = Some(ExternalServiceName("foo")))) must be(Seq.empty)
    }.response.status must be(400)
  }

}
