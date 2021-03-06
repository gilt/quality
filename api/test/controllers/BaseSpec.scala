package controllers

import com.gilt.quality.v0.models._
import db._
import java.util.UUID
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

abstract class BaseSpec extends PlaySpec with OneServerPerSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit override lazy val port = 8002
  implicit override lazy val app: FakeApplication = FakeApplication()

  val client = new com.gilt.quality.v0.Client(s"http://localhost:$port")

  def createUser(
    form: UserForm = createUserForm()
  ): User = {
    await(client.users.post(form))
  }

  def createUserForm() = UserForm(
    email = UUID.randomUUID.toString + "@gilttest.com"
  )

  def createOrganization(
    form: OrganizationForm = createOrganizationForm()
  ): Organization = {
    await(client.organizations.post(form))
  }

  def createOrganizationForm() = OrganizationForm(
    name = "Org " + UUID.randomUUID.toString
  )

  def createSubscription(
    form: SubscriptionForm = createSubscriptionForm()
  ): Subscription = {
    await(client.subscriptions.post(form))
  }

  def createSubscriptionForm(
    org: Organization = createOrganization(),
    user: User = createUser()
  ) = SubscriptionForm(
    organizationKey = org.key,
    userGuid = user.guid,
    publication = Publication.IncidentsCreate
  )

  def createTeam(
    org: Organization = createOrganization(),
    form: TeamForm = createTeamForm()
  ): Team = {
    await(client.teams.postByOrg(org = org.key, teamForm = form))
  }

  def createTeamForm(
    org: Organization = createOrganization()
  ) = TeamForm(key = "team-" + UUID.randomUUID.toString)

  def createIncident(
    org: Organization,
    form: Option[IncidentForm] = None
  ): Incident = {
    await(
      client.incidents.postByOrg(
        org = org.key,
        incidentForm = form.getOrElse(createIncidentForm())
      )
    )
  }

  def createIncidentForm() = IncidentForm(
    teamKey = None,
    severity = Severity.Low,
    summary = "Test",
    description = None,
    tags = None
  )

  def createPlan(
    org: Organization,
    form: Option[PlanForm] = None
  ): Plan = {
    await(
      client.plans.postByOrg(
        org = org.key,
        planForm = form.getOrElse(createPlanForm(org))
      )
    )
  }

  def createPlanForm(
    org: Organization = createOrganization(),
    incident: Option[Incident] = None
  ) = PlanForm(
    incidentId = incident.getOrElse(createIncident(org)).id,
    body = "Test plan"
  )

  def gradePlan(
    org: Organization,
    plan: Plan,
    grade: Int = 100
  ): Plan = {
    await(client.plans.putGradeByOrgAndId(org.key, plan.id, 100))
    await(client.plans.getByOrgAndId(org.key, plan.id))
  }

  def createMeeting(
    org: Organization,
    form: MeetingForm = createMeetingForm()
  ): Meeting = {
    await(client.meetings.postByOrg(org = org.key, meetingForm = form))
  }

  def createMeetingForm() = {
    MeetingForm(
      scheduledAt = (new DateTime()).plus(1)
    )
  }

  def createAgendaItem(
    org: Organization,
    form: Option[AgendaItemForm] = None
  ): AgendaItem = {
    await(
      client.agendaItems.postByOrg(
        org = org.key,
        agendaItemForm = form.getOrElse(createAgendaItemForm(org))
      )
    )
  }

  def createAgendaItemForm(
    org: Organization,
    incident: Option[Incident] = None,
    meeting: Option[Meeting] = None
  ) = {
    AgendaItemForm(
      incidentId = incident.getOrElse(createIncident(org)).id,
      meetingId = meeting.getOrElse(createMeeting(org)).id,
      task = Task.ReviewTeam
    )
  }

  def createAgendaItemForTeam(team: Team): AgendaItem = {
    val incident = createIncident(
      team.organization,
      Some(createIncidentForm.copy(teamKey = Some(team.key)))
    )
    createAgendaItem(
      team.organization,
      Some(createAgendaItemForm(team.organization, incident = Some(incident)))
    )
  }

  // TODO: Remove org
  def createIncidentForTeam(org: Organization, team: Team): Incident = {
    createIncident(
      org = org,
      form = Some(createIncidentForm().copy(teamKey = Some(team.key)))
    )
  }

  // TODO: Remove org
  def createPlanForIncident(org: Organization, incident: Incident): Plan = {
    createPlan(
      org = org,
      form = Some(
        PlanForm(
          incidentId = incident.id,
          body = "Test"
        )
      )
    )
  }

}
