package db

import java.util.UUID

object Util {

  val user = User(guid = UUID.randomUUID)

  val incidentForm = IncidentForm(
    team_key = "architecture",
    severity = "low",
    summary = "Something happened",
    description = None
  )

  def reportForm(incident: Option[Incident] = None): ReportForm = {
    ReportForm(
      incident_id = incident.getOrElse(IncidentsDao.create(user, incidentForm)).id,
      body = "test"
    )
  }

  def report(incident: Option[Incident] = None): Report = {
    ReportsDao.create(user, reportForm(incident))
  }

}
