package db

import com.gilt.quality.models.{Organization, Statistic, Team}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

object StatisticsDao {

  private val BaseQuery = """
      select teams.id, 
          teams.key as team_key, 
          organizations.key as organization_key, 
          organizations.name as organization_name, 
          count(all_incidents.id) as total_incidents, 
          count(plans.id) as total_plans, 
          count(all_incidents.id) - count(plans.id) as total_open_incidents,
          count(grades.id) as total_grades, 
          ceiling(avg(grades.score)) as average_grade 
      from teams 
      join organizations on organizations.deleted_at is null and organizations.id = teams.organization_id
      left join incidents as all_incidents on all_incidents.team_id = teams.id and all_incidents.deleted_at is null
      left join plans on plans.incident_id = all_incidents.id and plans.deleted_at is null 
      left join grades on grades.plan_id = plans.id and grades.deleted_at is null 
      where teams.deleted_at is null
  """

  def findAll(
    numberHours: Int,
    teamKey: Option[String] = None
  ): Seq[Statistic] = {
    
    val sql = Seq(
      Some(BaseQuery.trim),
      teamKey.map { v => "and teams.key = lower(trim({team_key}))" },
      Some("and all_incidents.created_at >= current_timestamp - ({number_hours} * interval '1 hour')"),
      Some("group by organizations.key, organizations.name, teams.id, teams.key"),
      Some("order by organizations.key, teams.key desc")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      teamKey.map { v => NamedParameter("team_key", toParameterValue(v)) },
      Some(NamedParameter("number_hours", toParameterValue(numberHours)))
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Statistic(
          team = Team(
            key = row[String]("team_key"),
            organization = Organization(
              key = row[String]("organization_key"),
              name = row[String]("organization_name")
            )
          ),
          totalGrades = row[Long]("total_grades"),
          averageGrade = row[Option[BigDecimal]]("average_grade").map{v => v.toInt},
          totalIncidents = row[Long]("total_incidents"),
          totalOpenIncidents = row[Long]("total_open_incidents"),
          totalPlans = row[Long]("total_plans"),
          plans = PlansDao.findAll(teamKey = Some(row[String]("team_key")))
        )
      }.toSeq
    }
  }

}
