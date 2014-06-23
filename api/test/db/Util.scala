package db

import java.util.UUID

object Util {

  val user = User(guid = UUID.randomUUID)

  def incidentForm() = {
    IncidentForm(
      team_key = "architecture",
      severity = "low",
      summary = "Something happened",
      description = None
    )
  }

  def incident(form: Option[IncidentForm] = None): Incident = {
    IncidentsDao.create(user, form.getOrElse(incidentForm()))
  }

  def reportForm(incident: Option[Incident] = None): ReportForm = {
    ReportForm(
      incident_id = incident.getOrElse(IncidentsDao.create(user, incidentForm)).id,
      body = "test"
    )
  }

  def report(form: Option[ReportForm] = None): Report = {
    ReportsDao.create(user, form.getOrElse(reportForm()))
  }

  def gradeForm(report: Option[Report] = None): GradeForm = {
    GradeForm(
      report_id = report.getOrElse(ReportsDao.create(user, reportForm())).id,
      score = 100
    )
  }

  def grade(form: Option[GradeForm] = None): Grade = {
    GradesDao.create(user, form.getOrElse(gradeForm()))
  }

}
