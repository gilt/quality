package controllers

import com.gilt.quality.v0.models.{Plan, PlanForm}
import com.gilt.quality.v0.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class PlansSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  "POST /:org/plans" in new WithServer {
    val incident = createIncident(org)
    val plan = createPlan(org, Some(PlanForm(incidentId = incident.id, body = "test")))
    plan.incidentId must be(incident.id)
    plan.body must be("test")
  }

  "POST /:org/plans validates empty body" in new WithServer {
    val incident = createIncident(org)

    intercept[ErrorsResponse] {
      createPlan(org, Some(PlanForm(incidentId = incident.id, body = "  ")))
    }.errors.map(_.message) must be (Seq("Plan body cannot be empty"))
  }

  "PUT /:org/plans/:id updates body" in new WithServer {
    val incident = createIncident(org)
    val form = PlanForm(incidentId = incident.id, body = "test")
    val plan = createPlan(org, Some(form))
    await(
      client.plans.putByOrgAndId(
        org = org.key,
        id = plan.id,
        planForm = form.copy(body = "updated")
      )
    )

    val updated = await(client.plans.getByOrgAndId(org.key, plan.id)).get
    updated.incidentId must be(incident.id)
    updated.body must be("updated")
  }

  "POST /:org/plans/:id validates empty body" in new WithServer {
    val incident = createIncident(org)
    val form = PlanForm(incidentId = incident.id, body = "test")
    val plan = createPlan(org, Some(form))
    intercept[ErrorsResponse] {
      await(
        client.plans.putByOrgAndId(
          org = org.key,
          id = plan.id,
          planForm = form.copy(body = "  ")
        )
      )
    }.errors.map(_.message) must be (Seq("Plan body cannot be empty"))

  }

  "GET /:org/plans" in new WithServer {
    val plan1 = createPlan(org)
    val plan2 = createPlan(org)

    await(client.plans.getByOrg(org.key, id = Some(-1))) must be(Seq.empty)
    await(client.plans.getByOrg(org.key, id = Some(plan1.id))).head must be(plan1)
    await(client.plans.getByOrg(org.key, id = Some(plan2.id))).head must be(plan2)
  }

  "GET /:org/plans/:id" in new WithServer {
    val plan = createPlan(org)
    await(client.plans.getByOrgAndId(org.key, plan.id)) must be(Some(plan))
    await(client.plans.getByOrgAndId(org.key, -1)) must be(None)
  }

  "GET /:org/plans by team key" in new WithServer {
    val team = createTeam(org)
    val incident = createIncident(
      org = org,
      form = Some(createIncidentForm().copy(teamKey = Some(team.key)))
    )

    val plan = createPlan(
      org = org,
      form = Some(
        PlanForm(
          incidentId = incident.id,
          body = "Test plan"
        )
      )
    )

    await(client.plans.getByOrg(org.key, teamKey = Some(team.key))).map(_.id) must be(Seq(plan.id))
    await(client.plans.getByOrg(org.key, teamKey = Some(UUID.randomUUID.toString))).map(_.id) must be(Seq.empty)
  }

  "PUT /:org/plans/:id/grade" in new WithServer {
    val plan = createPlan(org)
    plan.grade must be(None)
    await(client.plans.putGradeByOrgAndId(org.key, plan.id, 100))
    await(client.plans.getByOrgAndId(org.key, plan.id)).get.grade must be(Some(100))
  }

}
