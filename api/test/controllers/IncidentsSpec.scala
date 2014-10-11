package controllers

import com.gilt.quality.models.{Incident, IncidentForm, Severity}
import com.gilt.quality.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class IncidentsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global
  lazy val org = createOrganization()

  "POST /:org/incidents" in new WithServer {
    val team = createTeam(org)
    val incident = createIncident(
      org = org,
      form = Some(
        IncidentForm(
          teamKey = Some(team.key),
          severity = Severity.Low,
          summary = "Test",
          description = Some("desc"),
          tags = Seq("a", "b")
        )
      )
    )

    incident.severity must be(Severity.Low)
    incident.team.map(_.key) must be(Some(team.key))
    incident.summary must be("Test")
    incident.description must be(Some("desc"))
    incident.tags must be(Seq("a", "b"))
  }

  "POST /:org/incidents validates team exists" in new WithServer {
    val teamKey = UUID.randomUUID.toString
    intercept[ErrorsResponse] {
      createIncident(
        org = org,
        form = Some(createIncidentForm().copy(teamKey = Some(teamKey)))
      )
    }.errors.map(_.message) must be (Seq(s"Team[$teamKey] not found"))
  }

  "POST /:org/incidents validates severity" in new WithServer {
    intercept[ErrorsResponse] {
      createIncident(
        org = org,
        form = Some(createIncidentForm().copy(severity = Severity.UNDEFINED("foo")))
      )
    }.errors.map(_.message) must be (Seq("Invalid severity[foo]"))
  }

  "DELETE /:org/incidents/:id" in new WithServer {
    val incident = createIncident(org)
    await(client.incidents.deleteByOrgAndId(org.key, incident.id)) must be(Some(()))
    await(client.incidents.getByOrg(org.key, id = Some(incident.id))) must be(Seq.empty)
  }

  "PUT /:org/incidents/:id" in new WithServer {
    val team = createTeam(org)

    val incident = createIncident(
      org = org,
      form = Some(
        IncidentForm(
          teamKey = None,
          severity = Severity.Low,
          summary = "Test",
          description = None,
          tags = Seq.empty
        )
      )
    )

    incident.severity must be(Severity.Low)
    incident.team.map(_.key) must be(None)
    incident.summary must be("Test")
    incident.description must be(None)
    incident.tags must be(Seq.empty)

    val fetched = await(
      client.incidents.putByOrgAndId(
        org = org.key,
        id = incident.id,
        incidentForm = IncidentForm(
          teamKey = Some(team.key),
          severity = Severity.High,
          summary = "Test 2",
          description = Some("foo"),
          tags = Seq("a tag")
        )
      )
    )

    fetched.id must be(incident.id)
    fetched.severity must be(Severity.High)
    fetched.team.map(_.key) must be(Some(team.key))
    fetched.summary must be("Test 2")
    fetched.description must be(Some("foo"))
    fetched.tags must be(Seq("a tag"))
  }

  "GET /:org/incidents" in new WithServer {
    val incident1 = createIncident(org)
    val incident2 = createIncident(org)

    await(client.incidents.getByOrg(org.key, id = Some(-1))) must be(Seq.empty)
    await(client.incidents.getByOrg(org.key, id = Some(incident1.id))).head must be(incident1)
    await(client.incidents.getByOrg(org.key, id = Some(incident2.id))).head must be(incident2)
  }

  "GET /:org/incidents/:id" in new WithServer {
    val incident = createIncident(org)
    await(client.incidents.getByOrgAndId(org.key, incident.id)) must be(Some(incident))
    await(client.incidents.getByOrgAndId(org.key, -1)) must be(None)
  }

  "GET /:org/incidents includes plan if available" in new WithServer {
    val incident = createIncident(org)
    val plan = createPlanForIncident(org, incident)

    await(client.incidents.getByOrg(org.key, id = Some(incident.id))).head.plan must be(Some(plan))
  }

  "GET /:org/incidents respects teamKey filtering" in new WithServer {
    val team = createTeam(org)
    val incidentNoTeam = createIncident(org)
    val incidentWithTeam = createIncidentForTeam(org, team)

    await(client.incidents.getByOrg(org.key, teamKey = Some(UUID.randomUUID.toString))).map(_.id) must be(Seq.empty)
    await(client.incidents.getByOrg(org.key, teamKey = Some(team.key))).map(_.id) must be(Seq(incidentWithTeam.id))
  }

  "GET /:org/incidents respects hasTeam filtering" in new WithServer {
    val team = createTeam(org)
    val incidentNoTeam = createIncident(org)
    val incidentWithTeam = createIncidentForTeam(org, team)

    await(client.incidents.getByOrg(org.key, id = Some(incidentNoTeam.id), hasTeam = Some(false))).map(_.id) must be(Seq(incidentNoTeam.id))
    await(client.incidents.getByOrg(org.key, id = Some(incidentNoTeam.id), hasTeam = Some(true))).map(_.id) must be(Seq.empty)

    await(client.incidents.getByOrg(org.key, id = Some(incidentWithTeam.id), hasTeam = Some(false))).map(_.id) must be(Seq.empty)
    await(client.incidents.getByOrg(org.key, id = Some(incidentWithTeam.id), hasTeam = Some(true))).map(_.id) must be(Seq(incidentWithTeam.id))
  }

  "GET /:org/incidents respects hasPlan filtering" in new WithServer {
    val team = createTeam(org)
    val incidentNoPlan = createIncidentForTeam(org, team)
    val incidentWithPlan = createIncidentForTeam(org, team)
    createPlanForIncident(org, incidentWithPlan)

    await(client.incidents.getByOrg(org.key, id = Some(incidentNoPlan.id), hasPlan = Some(false))).map(_.id) must be(Seq(incidentNoPlan.id))
    await(client.incidents.getByOrg(org.key, id = Some(incidentNoPlan.id), hasPlan = Some(true))).map(_.id) must be(Seq.empty)

    await(client.incidents.getByOrg(org.key, id = Some(incidentWithPlan.id), hasPlan = Some(false))).map(_.id) must be(Seq.empty)
    await(client.incidents.getByOrg(org.key, id = Some(incidentWithPlan.id), hasPlan = Some(true))).map(_.id) must be(Seq(incidentWithPlan.id))
  }

}
