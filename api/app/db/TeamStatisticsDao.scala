package db

import quality.models.{TeamStatistic, Team}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class TeamStatisticForm(
  team_key: String,
  seconds: Long
)

object TeamStatisticForm {
  implicit val readsTeamStatisticForm = Json.reads[TeamForm]
}

object TeamStatisticsDao {

  private val BaseQuery = """
      select teams.id, 
          teams.key as team_key, 
          count(all_incidents.id) as total_incidents, 
          count(plans.id) as total_plans, 
          count(all_incidents.id) - count(plans.id) as total_open_incidents,
          count(grades.id) as total_grades, 
          ceiling(avg(grades.score)) as average_grade 
      from teams 
      left join incidents as all_incidents on all_incidents.team_id = teams.id and all_incidents.deleted_at is null
      left join plans on plans.incident_id = all_incidents.id and plans.deleted_at is null 
      left join grades on grades.plan_id = plans.id and grades.deleted_at is null 
      where teams.deleted_at is null
  """

  private val GroupByStatement = """
      group by teams.id, teams.key
  """

  def findByTeamKey(key: String): Option[TeamStatistic] = {
    findAll(key = Some(key.trim.toLowerCase)).headOption
  }

  def findAll(key: Option[String] = None,
              seconds: Option[Long] = None): Seq[TeamStatistic] = {
    val sql = Seq(
      Some(BaseQuery.trim),
      key.map { v => "and teams.key = {key}" },
      seconds.map { v => "and all_incidents.created_at >= current_timestamp - ({seconds} * interval '1 second')"}, //current_timestamp - '{seconds} second' doesn't work with anorm NamedParameter. The ' is treated as a scala symbol
      Some(GroupByStatement.trim),
      Some("order by teams.key desc")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      key.map { v => NamedParameter("key", toParameterValue(v.trim.toLowerCase)) },
      seconds.map { v => NamedParameter("seconds", toParameterValue(v))}
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        TeamStatistic(
          team = Team(
            key = row[String]("team_key")  
          ),
          totalGrades = row[Long]("total_grades"),
          averageGrade = row[Option[BigDecimal]]("average_grade").map{v => v.toInt},
          totalIncidents = row[Long]("total_incidents"),
          totalOpenIncidents = row[Long]("total_open_incidents"),
          totalPlans = row[Long]("total_plans")
        )
      }.toSeq
    }
  }

}
