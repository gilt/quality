package controllers

import com.gilt.quality.models.{IncidentOrganizationChange}
import com.gilt.quality.error.ErrorsResponse
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class IncidentOrganizationChangesSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "POST /incident_organization_changes" in new WithServer {
    val org1 = createOrganization()
    val org2 = createOrganization()

    val incident1 = createIncident(org1)
    val incident2 = createIncident(org2)

    await(client.incidents.getByOrgAndId(org1.key, incident1.id)).map(_.id) must be(Some(incident1.id))
    await(client.incidents.getByOrgAndId(org1.key, incident2.id)).map(_.id) must be(None)

    await(client.incidents.getByOrgAndId(org2.key, incident1.id)).map(_.id) must be(None)
    await(client.incidents.getByOrgAndId(org2.key, incident2.id)).map(_.id) must be(Some(incident2.id))

    await(
      client.incidentOrganizationChanges.post(
        IncidentOrganizationChange(
          incidentId = incident2.id,
          organizationKey = org1.key
        )
      )
    )

    await(client.incidents.getByOrgAndId(org1.key, incident1.id)).map(_.id) must be(Some(incident1.id))
    await(client.incidents.getByOrgAndId(org1.key, incident2.id)).map(_.id) must be(Some(incident2.id))

    await(client.incidents.getByOrgAndId(org2.key, incident1.id)).map(_.id) must be(None)
    await(client.incidents.getByOrgAndId(org2.key, incident2.id)).map(_.id) must be(None)
  }

  "POST /incident_organization_changes validates incident id" in new WithServer {
    val org = createOrganization()
    intercept[ErrorsResponse] {
      await(
        client.incidentOrganizationChanges.post(
          IncidentOrganizationChange(
            incidentId = 0,
            organizationKey = org.key
          )
        )
      )
    }.errors.map(_.message) must be(Seq("Incident 0 not found"))
  }

  "POST /incident_organization_changes validates organization key" in new WithServer {
    val org = createOrganization()
    val incident = createIncident(org)

    intercept[ErrorsResponse] {
      await(
        client.incidentOrganizationChanges.post(
          IncidentOrganizationChange(
            incidentId = incident.id,
            organizationKey = org.key + "2"
          )
        )
      )
    }.errors.map(_.message) must be(Seq(s"Organization ${org.key}2 not found"))
  }

  "POST /incident_organization_changes clears team" in new WithServer {
    val org1 = createOrganization()
    val org2 = createOrganization()
    val org2Team = createTeam(org2)

    val incident1 = createIncident(org1)
    val incident2 = createIncident(org2, Some(createIncidentForm().copy(teamKey = Some(org2Team.key))))

    await(
      client.incidentOrganizationChanges.post(
        IncidentOrganizationChange(
          incidentId = incident2.id,
          organizationKey = org1.key
        )
      )
    )

    await(client.incidents.getByOrgAndId(org1.key, incident2.id)).flatMap(_.team) must be(None)
  }

}
