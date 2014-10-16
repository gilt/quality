package db

import actors.{MeetingSchedule, DayOfWeek}
import com.gilt.quality.models._
import java.util.UUID

object Util {

  def createOrganization(): Organization = {
    OrganizationsDao.create(User.Default,
      OrganizationForm(
        name = UUID.randomUUID.toString
      )
    )
  }

  def createMeeting(
    org: Organization = createOrganization()
  ): Meeting = {
    val dateTime = MeetingSchedule(
      dayOfWeek = DayOfWeek.Thursday,
      beginningHourUTC = 15
    ).upcomingDates().head

    MeetingsDao.upsert(org, dateTime)
  }

  def createTeam(
    org: Organization = createOrganization(),
    form: TeamForm = createTeamForm()
  ): Team = {
    TeamsDao.create(User.Default, FullTeamForm(org, form))
  }

  def createTeamForm() = TeamForm(
    key = UUID.randomUUID.toString
  )

  def createIncident(
    org: Organization = createOrganization(),
    form: IncidentForm = createIncidentForm()
  ): Incident = {
    IncidentsDao.create(User.Default, FullIncidentForm(org, form))
  }

  def createIncidentForm() = IncidentForm(
    teamKey = None,
    severity = Severity.Low,
    summary = "Test",
    description = None,
    tags = Seq.empty
  )

  def createPlan(
    org: Organization = createOrganization(),
    form: Option[PlanForm] = None
  ): Plan = {
    val f = form.getOrElse(createPlanForm(org))
    val incident = IncidentsDao.findById(f.incidentId).getOrElse {
      sys.error(s"Could not find incident[${f.incidentId}]")
    }
    PlansDao.create(User.Default, FullPlanForm(org, incident, f))
  }

  def createPlanForm(org: Organization) = PlanForm(
    incidentId = createIncident(org).id,
    body = "Test"
  )

  def createGrade(
    plan: Plan,
    score: Int
  ) {
    GradesDao.upsert(User.Default, GradeForm(plan.id, score))
  }

}
