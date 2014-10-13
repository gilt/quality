package db

import actors.{MeetingSchedule, DayOfWeek}
import com.gilt.quality.models.{Incident, IncidentForm, Meeting, Organization, OrganizationForm, Severity}
import java.util.UUID

object Util {

  new play.core.StaticApplication(new java.io.File("."))

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

}
