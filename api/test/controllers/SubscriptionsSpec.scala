package controllers

import com.gilt.quality.models.{Publication, Subscription, SubscriptionForm}
import com.gilt.quality.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class SubscriptionsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  "POST /subscriptions" in new WithServer {
    val user = createUser()
    val subscription = createSubscription(
      SubscriptionForm(
        organizationKey = org.key,
        userGuid = user.guid,
        publication = Publication.AllNewIncidents
      )
    )

    subscription.organization.key must be(org.key)
    subscription.user.guid must be(user.guid)
    subscription.publication must be(Publication.AllNewIncidents)
  }

/*

  "POST /subscriptions allows setting key" in new WithServer {
    val name = UUID.randomUUID.toString
    val key = UUID.randomUUID.toString
    val org = createSubscription(SubscriptionForm(name = name, key = Some(key)))
    org.name must be(name)
    org.key must be(key)
  }

  "POST /subscriptions validates name is valid" in new WithServer {
    intercept[ErrorsResponse] {
      createSubscription(SubscriptionForm(name = "a"))
    }.errors.map(_.message) must be(Seq(s"name must be at least ${db.SubscriptionsDao.MinNameLength} characters"))
  }

  "POST /subscriptions validates key is valid" in new WithServer {
    val name = UUID.randomUUID.toString

    intercept[ErrorsResponse] {
      createSubscription(SubscriptionForm(name = name, key = Some("a")))
    }.errors.map(_.message) must be(Seq(s"Key must be at least ${db.SubscriptionsDao.MinKeyLength} characters"))

    intercept[ErrorsResponse] {
      createSubscription(SubscriptionForm(name = name, key = Some("a bad key")))
    }.errors.map(_.message) must be(Seq(s"Key must be in all lower case and contain alphanumerics only. A valid key would be: a-bad-key"))
  }

  "DELETE /subscriptions/:key" in new WithServer {
    val org = createSubscription()
    await(client.subscriptions.deleteByKey(org.key)) must be(Some(()))
    await(client.subscriptions.get(key = Some(org.key))) must be(Seq.empty)
  }

  "GET /subscriptions" in new WithServer {
    val org1 = createSubscription()
    val org2 = createSubscription()

    await(client.subscriptions.get(key = Some(UUID.randomUUID.toString))) must be(Seq.empty)
    await(client.subscriptions.get(key = Some(org1.key))).head must be(org1)
    await(client.subscriptions.get(key = Some(org2.key))).head must be(org2)
  }

  "GET /subscriptions/:key" in new WithServer {
    val org = createSubscription()
    await(client.subscriptions.getByKey(org.key)) must be(Some(org))
    await(client.subscriptions.getByKey(UUID.randomUUID.toString)) must be(None)
  }
 */
}
