package db

import com.gilt.quality.v0.models.{Organization, Statistic, Team, User}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import java.util.UUID

object StatisticsDao {

  private val BaseQuery = s"""
      select ${TeamsDao.select(Some("team"))},
          organizations.key as organization_key, 
          organizations.name as organization_name, 
          count(incidents.id) as total_incidents, 
          count(plans.id) as total_plans, 
          count(incidents.id) - count(plans.id) as total_open_incidents,
          count(grades.id) as total_grades, 
          ceiling(avg(grades.score)) as average_grade 
      from teams 
      join organizations on organizations.deleted_at is null and organizations.id = teams.organization_id
      left join incidents as incidents on incidents.team_id = teams.id and incidents.deleted_at is null and incidents.organization_id = teams.organization_id
      left join plans on plans.incident_id = incidents.id and plans.deleted_at is null 
      left join grades on grades.plan_id = plans.id and grades.deleted_at is null 
      where teams.deleted_at is null
        and teams.organization_id = {organization_id}
  """

  def findAll(
    org: Organization,
    userGuid: Option[UUID] = None,
    teamKey: Option[String] = None
  ): Seq[Statistic] = {
    OrganizationsDao.lookupId(org.key) match {
      case None => Seq.empty
      case Some(orgId) => {
        val sql = Seq(
          Some(BaseQuery.trim),
          teamKey.map { v => "and teams.key = lower(trim({team_key}))" },
          userGuid.map { v => "and teams.id in (select team_id from team_members where team_members.deleted_at is null and team_members.user_guid = {user_guid}::uuid)" },
          Some("group by organizations.key, organizations.name, teams.id, teams.key"),
          Some("order by organizations.key, teams.key")
        ).flatten.mkString("\n   ")

        val bind = Seq(
          Some(NamedParameter("organization_id", orgId)),
          teamKey.map { v => NamedParameter("team_key", toParameterValue(v)) },
          userGuid.map { v => NamedParameter("user_guid", toParameterValue(v)) }
        ).flatten

        DB.withConnection { implicit c =>
          SQL(sql).on(bind: _*)().toList.map { row =>
            Statistic(
              team = TeamsDao.fromRow(row, Some("team")),
              totalGrades = row[Long]("total_grades"),
              averageGrade = row[Option[BigDecimal]]("average_grade").map{v => v.toInt},
              totalIncidents = row[Long]("total_incidents"),
              totalOpenIncidents = row[Long]("total_open_incidents"),
              totalPlans = row[Long]("total_plans"),
              plans = Some(PlansDao.findAll(org = Some(org), teamKey = Some(row[String]("team_key"))))
            )
          }.toSeq
        }
      }
    }
  }
}
