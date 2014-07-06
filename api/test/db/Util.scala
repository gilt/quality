package db

import quality.models.{ Incident, Plan }
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
      severity = Incident.Severity.Low.toString,
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

  def planForm(incident: Option[Incident] = None): PlanForm = {
    PlanForm(
      incident_id = incident.getOrElse(createIncident()).id,
      body = "test"
    )
  }

  def createPlan(form: Option[PlanForm] = None): Plan = {
    PlansDao.create(user, form.getOrElse(planForm()))
  }

  def gradeForm(plan: Option[Plan] = None): GradeForm = {
    GradeForm(
      plan_id = createPlan().id,
      score = 100
    )
  }

  def createGrade(form: Option[GradeForm] = None): Grade = {
    GradesDao.create(user, form.getOrElse(gradeForm()))
  }

}
