package db

import org.joda.time.DateTime
import com.gilt.quality.models._
import java.util.UUID

object Util {

  val user = User(guid = UUID.randomUUID)
  lazy val testOrg = createOrganization()

  def createOrganization(
    form: OrganizationForm = createOrganizationForm()
  ): Organization = {
    OrganizationsDao.create(user, form)
  }

  def createOrganizationForm(): OrganizationForm = {
    OrganizationForm(
      name = "Test Org " + UUID.randomUUID.toString
    )
  }

  def teamForm() = {
    TeamForm(
      orgKey = createOrganization().key,
      key = "test-team"
    )
  }

  def upsertTeam(
    org: Organization = testOrg,
    key: String
  ) {
    TeamsDao.lookupId(org, key).getOrElse {
      TeamsDao.create(user, FullTeamForm(org, teamForm.copy(key = key)))
    }
  }

  def incidentForm() = {
    IncidentForm(
      team_key = Some("test-team"),
      severity = Severity.Low.toString,
      summary = "Something happened",
      description = None
    )
  }

  def createIncident(
    org: Organization = testOrg,
    form: IncidentForm = incidentForm()
  ): Incident = {
    form.team_key.map { Util.upsertTeam(org, _) }
    IncidentsDao.create(user, FullIncidentForm(org, form))
  }

  def incidentTagForm(incident: Option[Incident] = None): IncidentTagForm = {
    IncidentTagForm(
      incident_id = createIncident().id,
      tag = "app:product_service"
    )
  }

  def createIncidentTag(form: Option[IncidentTagForm] = None): IncidentTag = {
    IncidentTagsDao.create(user, form.getOrElse(incidentTagForm()))
  }

  def planForm(incident: Option[Incident] = None): PlanForm = {
    PlanForm(
      incident_id = incident.getOrElse(createIncident()).id,
      body = "test"
    )
  }

  def createPlan(form: Option[PlanForm] = None): Plan = {
    val plan = PlansDao.create(user, form.getOrElse(planForm()))
    form.map(_.grade).foreach { grade =>
      grade.foreach { score =>
        upsertGrade(Some(GradeForm(plan_id = plan.id, score = score)))
      }
    }
    PlansDao.findById(plan.id).get
  }

  def gradeForm(plan: Option[Plan] = None): GradeForm = {
    GradeForm(
      plan_id = createPlan().id,
      score = 100
    )
  }

  def upsertGrade(form: Option[GradeForm] = None): Grade = {
    GradesDao.upsert(user, form.getOrElse(gradeForm()))
  }

  def createMeetingForm() = {
    MeetingForm(
      scheduledAt = (new DateTime()).plus(1)
    )
  }

  def createMeeting(form: MeetingForm = createMeetingForm()): Meeting = {
    MeetingsDao.create(user, form)
  }

  def createAgendaItemForm() = {
    AgendaItemForm(
      incidentId = createIncident().id,
      task = Task.ReviewTeam
    )
  }

  def createAgendaItem(
    meeting: Meeting = createMeeting(),
    form: AgendaItemForm = createAgendaItemForm()
  ): AgendaItem = {
    AgendaItemsDao.create(user, AgendaItemFullForm(meeting, form))
  }
}
