package db

import quality.models.Event
import anorm._
import AnormHelper._
import anorm.ParameterValue._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

object EventsDao {

  private val qualityUrl = current.configuration.getString("quality.url").getOrElse {
    sys.error(s"configuration parameter[quality.url] is required")
  }

  private val ModelQueryTemplate = """
    select '%s' as model,
           %s.id as model_id,
           %s.updated_at as timestamp,
           case
             when deleted_at is not null then 'deleted'
             when %s.created_at + interval '1 second' >= %s.updated_at then 'created'
             else 'updated'
           end as action
      from %s
     order by %s.updated_at desc
     limit %s
  """

  private def modelQuery(model: String, table: String, maxRecords: Integer): String = {
    ModelQueryTemplate.format(model, table, table, table, table, table, table, maxRecords).trim
  }

  def findAll(
    limit: Int = 50,
    offset: Int = 0
  ): Seq[Event] = {

    val max = (offset*limit) + limit

    val sql = "select * from (\n" + 
      Seq(
        "(" + modelQuery("incident", "incidents", max) + ")",
        "(" + modelQuery("plan", "plans", max) + ")"
      ).mkString("\n UNION ALL \n") +
      s"\n) events order by events.timestamp desc limit ${limit} offset ${offset}"

    DB.withConnection { implicit c =>
      SQL(sql)().toList.map { row =>
        val model = Event.Model(row[String]("model"))
        val action = Event.Action(row[String]("action"))
        Event(
          model = model,
          action = action,
          timestamp = row[org.joda.time.DateTime]("timestamp"),
          url = buildUrl(action, model, row[Long]("model_id"))
        )
      }.toSeq
    }
  }

  private def buildUrl(action: Event.Action, model: Event.Model, id: Long): Option[String] = {

    action match {
      case Event.Action.Created | Event.Action.Updated => {
        model match {
          case Event.Model.Incident => Some(s"$qualityUrl/incidents/$id")
          case Event.Model.Plan => Some(s"$qualityUrl/plans/$id")
          case Event.Model.UNDEFINED(_) => None
        }
      }
      case Event.Action.Deleted | Event.Action.UNDEFINED(_) => {
        None
      }
    }
  }

}
