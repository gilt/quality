package db

import quality.models.{Statistic, Team}
import anorm._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class StatisticForm(
  team_key: String,
  num_days: Int
)

object StatisticForm {
  implicit val readsTeamStatisticForm = Json.reads[TeamForm]
}

object StatisticsDao {

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

  def findByTeamKey(key: String): Option[Statistic] = {
    findAll(key = Some(key.trim.toLowerCase)).headOption
  }

  def findAll(key: Option[String] = None,
              numDays: Option[Int] = None): Seq[Statistic] = {

    val numberDays: Option[Int] = numDays match {
      case None => Some(7)
      case Some(v: Int) => Some(v)
    }
    
    val sql = Seq(
      Some(BaseQuery.trim),
      key.map { v => "and teams.key = {key}" },
      Some("and all_incidents.created_at >= current_timestamp - ({numDays} * interval '1 day')"), //current_timestamp - '{seconds} second' doesn't work with anorm NamedParameter. The ' is treated as a scala symbol
      Some(GroupByStatement.trim),
      Some("order by teams.key desc")
    ).flatten.mkString("\n   ")

    val bind = Seq(
      key.map { v => NamedParameter("key", toParameterValue(v.trim.toLowerCase)) },
      numberDays.map { v => NamedParameter("numDays", toParameterValue(numberDays))}
    ).flatten

    DB.withConnection { implicit c =>
      SQL(sql).on(bind: _*)().toList.map { row =>
        Statistic(
          team = Team(
            key = row[String]("team_key")  
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
