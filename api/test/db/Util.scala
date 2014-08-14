package db

import com.gilt.quality.models.{ Incident, Plan, Severity }
import java.util.UUID

object Util {

  val user = User(guid = UUID.randomUUID)

  def teamForm() = {
    TeamForm(
      key = "test-team"
    )
  }

  def upsertTeam(key: String) {
    TeamsDao.lookupId(key).getOrElse {
      TeamsDao.create(user, TeamForm(key = key))
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

  def createIncident(form: Option[IncidentForm] = None): Incident = {
    val f = form.getOrElse(incidentForm())
    f.team_key.map { key => Util.upsertTeam(key) }
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

}
