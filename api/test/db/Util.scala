package db

import java.util.UUID

object Util {

  val user = User(guid = UUID.randomUUID)

  def teamForm() = {
    TeamForm(
      key = "architecture"
    )
  }

  def upsertTeam(key: String) {
    TeamsDao.lookupId(key).getOrElse {
      TeamsDao.create(user, TeamForm(key = key))
    }
  }

  def incidentForm() = {
    IncidentForm(
      team_key = "architecture",
      severity = "low",
      summary = "Something happened",
      description = None
    )
  }

  def createIncident(form: Option[IncidentForm] = None): Incident = {
    val f = form.getOrElse(incidentForm())
    Util.upsertTeam(f.team_key)
    IncidentsDao.create(user, f)
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

  def reportForm(incident: Option[Incident] = None): ReportForm = {
    ReportForm(
      incident_id = incident.getOrElse(createIncident()).id,
      body = "test"
    )
  }

  def createReport(form: Option[ReportForm] = None): Report = {
    ReportsDao.create(user, form.getOrElse(reportForm()))
  }

  def gradeForm(report: Option[Report] = None): GradeForm = {
    GradeForm(
      report_id = createReport().id,
      score = 100
    )
  }

  def createGrade(form: Option[GradeForm] = None): Grade = {
    GradesDao.create(user, form.getOrElse(gradeForm()))
  }

}
