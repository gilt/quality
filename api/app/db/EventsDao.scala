package db

import com.gilt.quality.models.{Action, Event, EventData, Model, Organization}
import anorm._
import AnormHelper._
import anorm.ParameterValue._
import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

object EventsDao {

  private val IncidentsQuery = """
    select 'incident' as model,
           incidents.id as model_id,
           incidents.updated_at as timestamp,
           incidents.summary as summary,
           case
             when incidents.deleted_at is not null then 'deleted'
             when incidents.created_at + interval '1 second' >= incidents.updated_at then 'created'
             else 'updated'
           end as action
      from incidents
     where incidents.organization_id = {organization_id}
     order by incidents.updated_at desc
     limit %s
  """

  private val PlansQuery = """
    select 'plan' as model,
           plans.id as model_id,
           plans.updated_at as timestamp,
           'Plan for Incident #' || incidents.id || ': ' || incidents.summary as summary,
           case
             when plans.deleted_at is not null then 'deleted'
             when plans.created_at + interval '1 second' >= plans.updated_at then 'created'
             else 'updated'
           end as action
      from plans
      join incidents on incidents.id = plans.incident_id and incidents.organization_id = {organization_id}
     order by plans.updated_at desc
     limit %s
  """

  private val GradesQuery = """
    select 'rating' as model,
           plans.id as model_id,
           grades.updated_at as timestamp,
           'Rating for Incident #' || incidents.id || ': ' || incidents.summary as summary,
           case
             when grades.deleted_at is not null then 'deleted'
             when grades.created_at + interval '1 second' >= grades.updated_at then 'created'
             else 'updated'
           end as action
      from grades
      join plans on plans.id = grades.plan_id
      join incidents on incidents.id = plans.incident_id and incidents.organization_id = {organization_id}
     order by grades.updated_at desc
     limit %s
  """

  def findAll(
    org: Organization,
    model: Option[Model] = None,
    action: Option[Action] = None,
    numberHours: Option[Int] = None,
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Event] = {

    val max = (offset*limit) + limit

    val modelClause = model match {
      case None => "true"
      case Some(m) => s"events.model = '${m.toString}'"
    }

    val actionClause = action match {
      case None => "true"
      case Some(m) => s"events.action = '${m.toString}'"
    }

    val numberHoursClause = numberHours match {
      case None => "true"
      case Some(n: Int) => s"events.timestamp > now() - interval '$n hours'"
    }

    val sql = "select * from (\n" + 
      Seq(
        "(" + IncidentsQuery.format(max) + ")",
        "(" + PlansQuery.format(max) + ")",
        "(" + GradesQuery.format(max) + ")"
      ).mkString("\n UNION ALL \n") +
      s"\n) events where $modelClause and $actionClause and $numberHoursClause order by events.timestamp desc limit ${limit} offset ${offset}"

    val orgId = OrganizationsDao.lookupId(org.key).getOrElse {
      sys.error(s"Could not find id for org[${org.key}]")
    }

    DB.withConnection { implicit c =>
      SQL(sql).on('organization_id -> orgId)().toList.map { row =>
        val model = Model(row[String]("model"))
        val modelId = row[Long]("model_id")
        val action = Action(row[String]("action"))

        Event(
          model = model,
          action = action,
          timestamp = row[DateTime]("timestamp"),
          url = buildUrl(action, model, modelId),
          data = EventData(
            modelId = modelId,
            summary = row[String]("summary")
          )
        )
      }.toSeq
    }
  }

  private def buildUrl(action: Action, model: Model, id: Long): Option[String] = {

    action match {
      case Action.Created | Action.Updated => {
        model match {
          case Model.Incident => Some(s"/incidents/$id")
          case Model.Plan => Some(s"/plans/$id")
          case Model.Rating => Some(s"/plans/$id")
          case Model.UNDEFINED(_) => None
        }
      }
      case Action.Deleted | Action.UNDEFINED(_) => {
        None
      }
    }
  }

}
