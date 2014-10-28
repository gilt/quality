package controllers

import db.ExternalServicesDao
import com.gilt.quality.models.{ExternalService, ExternalServiceForm, ExternalServiceName, Organization}
import com.gilt.quality.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class ExternalServicesSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  def createExternalService(
    org: Organization,
    name: ExternalServiceName = ExternalServiceName.Jira,
    url: String = "http://localhost"
  ): ExternalService = {
    await(
      client.externalServices.postExternalServicesByOrg(
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

/*
  "POST /:org/external_services" in new WithServer {
    val key = UUID.randomUUID.toString
    val service = createExternalService(org, ExternalServiceName.Jira)
    service.name must be(ExternalServiceName.Jira)
    service.url must be("http://localhost")
    service.username must be("test")
    ExternalServicesDao.lookupPassword(service) must be("password")
  }
 */

  "POST /:org/external_services validates name" in new WithServer {
    val key = UUID.randomUUID.toString
    intercept[ErrorsResponse] {
      createExternalService(org, ExternalServiceName("foo"))
    }.errors.map(_.message) must be(Seq("Unrecognized external service[foo]"))
  }

  "POST /:org/external_services validates url" in new WithServer {
    val key = UUID.randomUUID.toString
    intercept[ErrorsResponse] {
      createExternalService(org, url = "foo")
    }.errors.map(_.message) must be(Seq("URL must start with http"))
  }

/*
  "POST /:org/external_services w/ email address" in new WithServer {
    val email = UUID.randomUUID.toString + "@quality.mailinator.com"
    val externalService = createExternalService(org, ExternalServiceForm(key = UUID.randomUUID.toString, email = Some(email)))
    externalService.email must be(Some(email))

    intercept[ErrorsResponse] {
      createExternalService(org, ExternalServiceForm(key = UUID.randomUUID.toString, email = Some("bad")))
    }.errors.map(_.message) must be (Seq("Email address is not valid"))
  }

  "POST /:org/external_services w/ icons" in new WithServer {
    val smileyUrl = "http://localhost/s.jpg"
    val frownyUrl = "http://localhost/f.jpg"

    val externalService = createExternalService(
      org,
      ExternalServiceForm(
        key = UUID.randomUUID.toString, 
        smileyUrl = Some(smileyUrl),
        frownyUrl = Some(frownyUrl)
      )
    )

    externalService.icons.smileyUrl must be(smileyUrl)
    externalService.icons.frownyUrl must be(frownyUrl)
  }

  "PUT /:org/external_services updates icons" in new WithServer {
    val smileyUrl = "http://localhost/s.jpg"
    val frownyUrl = "http://localhost/f.jpg"

    val externalService = createExternalService(org)
    externalService.icons must be(Defaults.Icons)

    val updated = await(
      client.externalServices.putByOrgAndKey(
        org = org.key,
        key = externalService.key,
        updateExternalServiceForm = UpdateExternalServiceForm(
          smileyUrl = Some(smileyUrl),
          frownyUrl = Some(frownyUrl)
        )
      )
    )

    updated.icons.smileyUrl must be(smileyUrl)
    updated.icons.frownyUrl must be(frownyUrl)
  }

  "PUT /:org/external_services updates email" in new WithServer {
    val externalService = createExternalService(org)
    externalService.email must be(None)

    val updated = await(
      client.externalServices.putByOrgAndKey(
        org = org.key,
        key = externalService.key,
        updateExternalServiceForm = UpdateExternalServiceForm(
          email = Some("foo@gilt.com")
        )
      )
    )

    updated.email must be(Some("foo@gilt.com"))
  }

  "POST /:org/external_services use default icons" in new WithServer {
    createExternalService(org).icons must be(Defaults.Icons)
  }

  "POST /:org/external_services validates that key cannot be reused" in new WithServer {
    val externalService = createExternalService(org)

    intercept[ErrorsResponse] {
      createExternalService(org, ExternalServiceForm(key = externalService.key))
    }.errors.map(_.message) must be (Seq(s"ExternalService with key[${externalService.key}] already exists"))
  }

  "DELETE /:org/external_services/:key" in new WithServer {
    val externalService = createExternalService(org)
    await(client.externalServices.deleteByOrgAndKey(org.key, externalService.key)) must be(Some(()))
    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key))) must be(Seq.empty)
  }

  "GET /:org/external_services" in new WithServer {
    val externalService1 = createExternalService(org)
    val externalService2 = createExternalService(org)

    await(client.externalServices.getByOrg(org.key, key = Some(UUID.randomUUID.toString))) must be(Seq.empty)
    await(client.externalServices.getByOrg(org.key, key = Some(externalService1.key))).head must be(externalService1)
    await(client.externalServices.getByOrg(org.key, key = Some(externalService2.key))).head must be(externalService2)
  }

  "GET /:org/external_services/:key" in new WithServer {
    val externalService = createExternalService(org)
    await(client.externalServices.getByOrgAndKey(org.key, externalService.key)) must be(Some(externalService))
    await(client.externalServices.getByOrgAndKey(org.key, UUID.randomUUID.toString)) must be(None)
  }

  "GET /:org/external_services by userGuid" in new WithServer {
    val externalService = createExternalService(org)
    val user1 = createUser()
    val user2 = createUser()

    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user1.guid))

    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key), userGuid = Some(UUID.randomUUID))).map(_.key) must be(Seq.empty)
    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key), userGuid = Some(user1.guid))).map(_.key) must be(Seq(externalService.key))
    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key), userGuid = Some(user2.guid))).map(_.key) must be(Seq.empty)
  }

  "GET /:org/external_services by excludeUserGuid" in new WithServer {
    val externalService = createExternalService(org)
    val user1 = createUser()
    val user2 = createUser()

    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user1.guid))

    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key), excludeUserGuid = Some(UUID.randomUUID))).map(_.key) must be(Seq(externalService.key))
    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key), excludeUserGuid = Some(user1.guid))).map(_.key) must be(Seq.empty)
    await(client.externalServices.getByOrg(org.key, key = Some(externalService.key), excludeUserGuid = Some(user2.guid))).map(_.key) must be(Seq(externalService.key))
  }

  "PUT /:org/external_services/:key/members/:user_guid" in new WithServer {
    val externalService = createExternalService(org)
    val user = createUser()
    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key)).map(_.user.guid) must be(Seq.empty)

    val member = await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user.guid))
    member.externalService must be(externalService)
    member.user must be(user)

    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key)).map(_.user.guid) must be(Seq(user.guid))
  }

  "DELETE /:org/external_services/:key/members/:user_guid" in new WithServer {
    val externalService = createExternalService(org)
    val user = createUser()

    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user.guid))
    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key)).map(_.user.guid) must be(Seq(user.guid))

    await(client.externalServices.deleteMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user.guid))
    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key)).map(_.user.guid) must be(Seq.empty)

    await(client.externalServices.deleteMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user.guid))
    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key)).map(_.user.guid) must be(Seq.empty)
  }

  "GET /:org/external_services/:key/members" in new WithServer {
    val externalService = createExternalService(org)
    val user1 = createUser()
    val user2 = createUser()

    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user1.guid))
    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user2.guid))

    await(client.externalServices.getMembersByOrgAndKey(org.key, UUID.randomUUID.toString)).map(_.user.guid) must be(Seq.empty)
    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key)).map(_.user.guid).sorted must be(Seq(user1.guid, user2.guid).sorted)

    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key, userGuid = Some(UUID.randomUUID))).map(_.user.guid) must be(Seq.empty)
    await(client.externalServices.getMembersByOrgAndKey(org.key, externalService.key, userGuid = Some(user1.guid))).map(_.user.guid) must be(Seq(user1.guid))
  }

  "GET /:org/external_services/:key/member_summary" in new WithServer {
    val externalService = createExternalService(org)
    val otherExternalService = createExternalService(org)
    val user1 = createUser()
    val user2 = createUser()

    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, externalService.key)).get.numberMembers must be(0)
    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, externalService.key)).get.numberMembers must be(0)

    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user1.guid))
    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, otherExternalService.key)).get.numberMembers must be(0)
    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, externalService.key)).get.numberMembers must be(1)

    await(client.externalServices.putMembersByOrgAndKeyAndUserGuid(org.key, externalService.key, user2.guid))
    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, otherExternalService.key)).get.numberMembers must be(0)
    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, externalService.key)).get.numberMembers must be(2)

    await(client.externalServices.getMemberSummaryByOrgAndKey(org.key, UUID.randomUUID.toString)) must be(None)
  }
 */
}
