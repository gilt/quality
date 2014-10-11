package controllers

import com.gilt.quality.models.{Action, Event, Model}

import play.api.test._
import play.api.test.Helpers._

class EventsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  "GET /:org/events" in new WithServer {
    val incident1 = createIncident(org)
    val incident2 = createIncident(org)

    await(client.events.getByOrg(org.key)).map(_.model) must be(Seq(Model.Incident, Model.Incident))

    await(client.incidents.deleteByOrgAndId(org.key, incident1.id))
    val events = await(client.events.getByOrg(org.key))
    events.map(_.model) must be(Seq(Model.Incident, Model.Incident))
    events.head.action must be(Action.Deleted)

    //createPlan()
    //await(client.events.getByOrg(org.key)).head.model must be(Model.Plan)
  }

}
