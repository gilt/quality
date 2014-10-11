package controllers

import com.gilt.quality.models.{Incident, Plan, PlanForm, Team}
import com.gilt.quality.error.ErrorsResponse
import java.util.UUID
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._

class StatisticsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  def createIncidentForTeam(team: Team): Incident = {
    createIncident(
      org = org,
      form = Some(createIncidentForm(org).copy(teamKey = Some(team.key)))
    )
  }

  def createPlanForTeam(team: Team): Plan = {
    createPlan(
      org = org,
      form = Some(
        PlanForm(
          incidentId = createIncidentForTeam(team).id,
          body = "Test"
        )
      )
    )
  }

  def verifyStats(
    team: Team,
    totalGrades: Int,
    averageGrade: Int,
    totalIncidents: Int,
    totalOpenIncidents: Int,
    totalPlans: Int
  ) {

    val statistics = await(
      client.statistics.getByOrg(
        org = org.key,
        teamKey = Some(team.key),
        numberHours = Some(24)
      )
    ).filter(_.team.key == team.key)

    statistics.map(_.team.key) must be(Seq(team.key))
    val statistic = statistics.head

    statistic.team.key must be(team.key)
    statistic.totalGrades must be(totalGrades)
    statistic.averageGrade must be(Some(averageGrade))
    statistic.totalIncidents must be(totalIncidents)
    statistic.totalOpenIncidents must be(totalOpenIncidents)
    statistic.totalPlans must be(totalPlans)
  }


  "GET /:org/statistics" in new WithServer {
    val team = createTeam(org)

    val plan1 = createPlanForTeam(team)
    gradePlan(org, plan1, 100)
    verifyStats(team, 1, 100, 1, 0, 1)

    val plan2 = createPlanForTeam(team)
    verifyStats(team, 1, 100, 2, 0, 2)
    gradePlan(org, plan2, 20)
    verifyStats(team, 2, 100, 2, 0, 2)

    val openIncident = createIncidentForTeam(team)
    verifyStats(team, 2, 100, 3, 1, 2)

    val plan3 = createPlanForTeam(team)
    verifyStats(team, 2, 100, 4, 1, 3)
  }

}
