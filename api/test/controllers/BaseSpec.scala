package controllers

import com.gilt.quality.models._
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

  val client = new com.gilt.quality.Client(s"http://localhost:$port")

  def createOrganization(
    form: OrganizationForm = createOrganizationForm()
  ): Organization = {
    await(client.organizations.post(form))
  }

  def createOrganizationForm() = OrganizationForm(
    name = "Org " + UUID.randomUUID.toString
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
    form: IncidentForm = createIncidentForm()
  ): Incident = {
    await(client.incidents.post(form))
  }

  def createIncidentForm(
    org: Organization = createOrganization()
  ) = IncidentForm(
    teamKey = None,
    severity = Severity.Low,
    summary = "Test",
    description = None,
    tags = Seq.empty
  )

  def createMeeting(
    form: MeetingForm = createMeetingForm()
  ): Meeting = {
    await(client.meetings.post(form))
  }

  def createMeetingForm() = {
    MeetingForm(
      scheduledAt = (new DateTime()).plus(1)
    )
  }

  def createAgendaItem(
    meeting: Meeting = createMeeting(),
    form: AgendaItemForm = createAgendaItemForm()
  ): AgendaItem = {
    await(
      client.agendaItems.post(
        meetingId = meeting.id,
        agendaItemForm = form
      )
    )
  }

  def createAgendaItemForm() = {
    AgendaItemForm(
      incidentId = createIncident().id,
      task = Task.ReviewTeam
    )
  }

}
