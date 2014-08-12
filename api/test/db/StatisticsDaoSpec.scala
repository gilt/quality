package db

import quality.models.{Incident, Plan, Severity, Statistic, Team}
import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class StatisticsDaoSpec extends FunSpec with Matchers {

  it("should find by existing team key") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString
      Util.upsertTeam(teamKey)

      val incident = Util.createIncident(Some(IncidentForm(team_key = Some(teamKey), severity = Severity.High.toString, summary = "test")))
      val grade = Util.upsertGrade(Some(GradeForm(plan_id = Util.createPlan(Some(PlanForm(incident_id = incident.id, body = "test"))).id, score = 100)))
      val other = Util.createPlan()

      val statistic = StatisticsDao.findAll(numberHours = 24, teamKey = Some(teamKey)).head
      statistic.team should be(Team(teamKey))
      statistic.totalGrades should be(1)
      statistic.averageGrade should be(Some(100))
      statistic.totalIncidents should be(1)
      statistic.totalOpenIncidents should be(0)
      statistic.totalPlans should be(1)
    }
  }

}
