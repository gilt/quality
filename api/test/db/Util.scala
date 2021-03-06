package db

import actors.{MeetingSchedule, DayOfWeek}
import com.gilt.quality.v0.models._
import java.util.UUID

object Util {

  def createOrganization(): Organization = {
    OrganizationsDao.create(UsersDao.Default,
      OrganizationForm(
        name = UUID.randomUUID.toString
      )
    )
  }

  def createMeeting(
    org: Organization = createOrganization()
  ): Meeting = {
    val dateTime = MeetingSchedule.newYork(
      dayOfWeek = DayOfWeek.Thursday,
      beginningHour = 15
    ).upcomingDates().head

    MeetingsDao.upsert(org, dateTime)
  }

   def createTeam(
    org: Organization = createOrganization(),
    form: TeamForm = createTeamForm()
  ): Team = {
    TeamsDao.create(UsersDao.Default, FullTeamForm(org, form))
  }

  def createTeamForm() = TeamForm(
    key = UUID.randomUUID.toString
  )

   def createUser(
    form: UserForm = createUserForm()
  ): User = {
    UsersDao.create(UsersDao.Default, form)
  }

  def createUserForm() = UserForm(
    email = UUID.randomUUID.toString + "@gilttest.com"
  )

  def createIncident(
    org: Organization = createOrganization(),
    form: IncidentForm = createIncidentForm()
  ): Incident = {
    IncidentsDao.create(UsersDao.Default, FullIncidentForm(org, form))
  }

  def createIncidentForm() = IncidentForm(
    teamKey = None,
    severity = Severity.Low,
    summary = "Test",
    description = None,
    tags = None
  )

  def createPlan(
    org: Organization = createOrganization(),
    form: Option[PlanForm] = None
  ): Plan = {
    val f = form.getOrElse(createPlanForm(org))
    val incident = IncidentsDao.findById(f.incidentId).getOrElse {
      sys.error(s"Could not find incident[${f.incidentId}]")
    }
    PlansDao.create(UsersDao.Default, FullPlanForm(org, incident, f))
  }

  def createPlanForm(org: Organization) = PlanForm(
    incidentId = createIncident(org).id,
    body = "Test"
  )

  def createGrade(
    plan: Plan,
    score: Int
  ) {
    GradesDao.upsert(UsersDao.Default, GradeForm(plan.id, score))
  }

  def createSubscription(
    org: Organization,
    user: User,
    publication: Publication
  ): Subscription = {
    SubscriptionsDao.create(
      UsersDao.Default,
      SubscriptionForm(
        organizationKey = org.key,
        userGuid = user.guid,
        publication = publication
      )
    )
  }

}
