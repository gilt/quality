package db

import quality.models.{ Event, EventData }
import anorm._
import AnormHelper._
import anorm.ParameterValue._
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
      join incidents on incidents.id = plans.incident_id
     order by plans.updated_at desc
     limit %s
  """

  def findAll(
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Event] = {

    val max = (offset*limit) + limit

    val sql = "select * from (\n" + 
      Seq(
        "(" + IncidentsQuery.format(max) + ")",
        "(" + PlansQuery.format(max) + ")"
      ).mkString("\n UNION ALL \n") +
      s"\n) events order by events.timestamp desc limit ${limit} offset ${offset}"

    DB.withConnection { implicit c =>
      SQL(sql)().toList.map { row =>
        val model = Event.Model(row[String]("model"))
        val modelId = row[Long]("model_id")
        val action = Event.Action(row[String]("action"))

        Event(
          model = model,
          action = action,
          timestamp = row[org.joda.time.DateTime]("timestamp"),
          url = buildUrl(action, model, modelId),
          data = EventData(
            modelId = modelId,
            summary = row[String]("summary")
          )
        )
      }.toSeq
    }
  }

  private def buildUrl(action: Event.Action, model: Event.Model, id: Long): Option[String] = {

    action match {
      case Event.Action.Created | Event.Action.Updated => {
        model match {
          case Event.Model.Incident => Some(s"/incidents/$id")
          case Event.Model.Plan => Some(s"/plans/$id")
          case Event.Model.UNDEFINED(_) => None
        }
      }
      case Event.Action.Deleted | Event.Action.UNDEFINED(_) => {
        None
      }
    }
  }

}
