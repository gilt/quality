package controllers

import com.gilt.quality.v0.models.{Incident, Organization, Plan, PlanForm, Team}
import com.gilt.quality.v0.error.ErrorsResponse
import java.util.UUID
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._

class StatisticsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  def createPlanForTeam(org: Organization, team: Team): Plan = {
    val incident = createIncidentForTeam(org, team)
    createPlanForIncident(org, incident)
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

    val plan1 = createPlanForTeam(org, team)
    gradePlan(org, plan1, 100)
    verifyStats(team, 1, 100, 1, 0, 1)

    val plan2 = createPlanForTeam(org, team)
    verifyStats(team, 1, 100, 2, 0, 2)
    gradePlan(org, plan2, 20)
    verifyStats(team, 2, 100, 2, 0, 2)

    val openIncident = createIncidentForTeam(org, team)
    verifyStats(team, 2, 100, 3, 1, 2)

    val plan3 = createPlanForTeam(org, team)
    verifyStats(team, 2, 100, 4, 1, 3)
  }

}
