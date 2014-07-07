package quality.models {
  case class Error(
    code: String,
    message: String
  )
  case class Event(
    model: Event.Model,
    action: Event.Action,
    timestamp: org.joda.time.DateTime,
    url: scala.Option[String] = None,
    data: EventData
  )
  object Event {

    sealed trait Model

    object Model {

      case object Incident extends Model { override def toString = "incident" }
      case object Plan extends Model { override def toString = "plan" }
      case object Rating extends Model { override def toString = "rating" }

      /**
       * UNDEFINED captures values that are sent either in error or
       * that were added by the server after this library was
       * generated. We want to make it easy and obvious for users of
       * this library to handle this case gracefully.
       *
       * We use all CAPS for the variable name to avoid collisions
       * with the camel cased values above.
       */
      case class UNDEFINED(override val toString: String) extends Model

      /**
       * all returns a list of all the valid, known values. We use
       * lower case to avoid collisions with the camel cased values
       * above.
       */
      val all = Seq(Incident, Plan, Rating)

      private[this]
      val byName = all.map(x => x.toString -> x).toMap

      def apply(value: String): Model = fromString(value).getOrElse(UNDEFINED(value))

      def fromString(value: String): Option[Model] = byName.get(value)

    }

    sealed trait Action

    object Action {

      case object Created extends Action { override def toString = "created" }
      case object Updated extends Action { override def toString = "updated" }
      case object Deleted extends Action { override def toString = "deleted" }

      /**
       * UNDEFINED captures values that are sent either in error or
       * that were added by the server after this library was
       * generated. We want to make it easy and obvious for users of
       * this library to handle this case gracefully.
       *
       * We use all CAPS for the variable name to avoid collisions
       * with the camel cased values above.
       */
      case class UNDEFINED(override val toString: String) extends Action

      /**
       * all returns a list of all the valid, known values. We use
       * lower case to avoid collisions with the camel cased values
       * above.
       */
      val all = Seq(Created, Updated, Deleted)

      private[this]
      val byName = all.map(x => x.toString -> x).toMap

      def apply(value: String): Action = fromString(value).getOrElse(UNDEFINED(value))

      def fromString(value: String): Option[Action] = byName.get(value)

    }
  }
  case class EventData(
    modelId: Long,
    summary: String
  )
  case class Healthcheck(
    status: String
  )
  case class Incident(
    id: Long,
    summary: String,
    description: scala.Option[String] = None,
    team: scala.Option[Team] = None,
    severity: Incident.Severity,
    tags: scala.collection.Seq[String] = Nil,
    plan: scala.Option[Plan] = None,
    createdAt: org.joda.time.DateTime
  )
  object Incident {

    sealed trait Severity

    object Severity {

      case object Low extends Severity { override def toString = "low" }
      case object High extends Severity { override def toString = "high" }

      /**
       * UNDEFINED captures values that are sent either in error or
       * that were added by the server after this library was
       * generated. We want to make it easy and obvious for users of
       * this library to handle this case gracefully.
       *
       * We use all CAPS for the variable name to avoid collisions
       * with the camel cased values above.
       */
      case class UNDEFINED(override val toString: String) extends Severity

      /**
       * all returns a list of all the valid, known values. We use
       * lower case to avoid collisions with the camel cased values
       * above.
       */
      val all = Seq(Low, High)

      private[this]
      val byName = all.map(x => x.toString -> x).toMap

      def apply(value: String): Severity = fromString(value).getOrElse(UNDEFINED(value))

      def fromString(value: String): Option[Severity] = byName.get(value)

    }
  }
  case class Plan(
    id: Long,
    incidentId: Long,
    body: String,
    grade: scala.Option[Int] = None,
    createdAt: org.joda.time.DateTime
  )
  case class Team(
    key: String
  )
}

package quality.models {
  package object json {
    import play.api.libs.json._
    import play.api.libs.functional.syntax._

    implicit val jsonReadsUUID = __.read[String].map(java.util.UUID.fromString)

    implicit val jsonWritesUUID = new Writes[java.util.UUID] {
      def writes(x: java.util.UUID) = JsString(x.toString)
    }

    implicit val jsonReadsJodaDateTime = __.read[String].map { str =>
      import org.joda.time.format.ISODateTimeFormat.dateTimeParser
      dateTimeParser.parseDateTime(str)
    }

    implicit val jsonWritesJodaDateTime = new Writes[org.joda.time.DateTime] {
      def writes(x: org.joda.time.DateTime) = {
        import org.joda.time.format.ISODateTimeFormat.dateTime
        val str = dateTime.print(x)
        JsString(str)
      }
    }

    implicit val jsonReadsEvent_Model = __.read[String].map(Event.Model.apply)
    
    implicit val jsonWritesEvent_Model = new Writes[Event.Model] {
      def writes(x: Event.Model) = JsString(x.toString)
    }
    
    implicit val jsonReadsEvent_Action = __.read[String].map(Event.Action.apply)
    
    implicit val jsonWritesEvent_Action = new Writes[Event.Action] {
      def writes(x: Event.Action) = JsString(x.toString)
    }
    
    implicit val jsonReadsIncident_Severity = __.read[String].map(Incident.Severity.apply)
    
    implicit val jsonWritesIncident_Severity = new Writes[Incident.Severity] {
      def writes(x: Incident.Severity) = JsString(x.toString)
    }

    implicit def readsError: play.api.libs.json.Reads[Error] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "code").read[String] and
         (__ \ "message").read[String])(Error.apply _)
      }
    
    implicit def writesError: play.api.libs.json.Writes[Error] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "code").write[String] and
         (__ \ "message").write[String])(unlift(Error.unapply))
      }
    
    implicit def readsEvent: play.api.libs.json.Reads[Event] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "model").read[Event.Model] and
         (__ \ "action").read[Event.Action] and
         (__ \ "timestamp").read[org.joda.time.DateTime] and
         (__ \ "url").readNullable[String] and
         (__ \ "data").read[EventData])(Event.apply _)
      }
    
    implicit def writesEvent: play.api.libs.json.Writes[Event] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "model").write[Event.Model] and
         (__ \ "action").write[Event.Action] and
         (__ \ "timestamp").write[org.joda.time.DateTime] and
         (__ \ "url").write[scala.Option[String]] and
         (__ \ "data").write[EventData])(unlift(Event.unapply))
      }
    
    implicit def readsEventData: play.api.libs.json.Reads[EventData] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "model_id").read[Long] and
         (__ \ "summary").read[String])(EventData.apply _)
      }
    
    implicit def writesEventData: play.api.libs.json.Writes[EventData] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "model_id").write[Long] and
         (__ \ "summary").write[String])(unlift(EventData.unapply))
      }
    
    implicit def readsHealthcheck: play.api.libs.json.Reads[Healthcheck] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        (__ \ "status").read[String].map { x =>
          new Healthcheck(status = x)
        }
      }
    
    implicit def writesHealthcheck: play.api.libs.json.Writes[Healthcheck] =
      new play.api.libs.json.Writes[Healthcheck] {
        def writes(x: Healthcheck) = play.api.libs.json.Json.obj(
          "status" -> play.api.libs.json.Json.toJson(x.status)
        )
      }
    
    implicit def readsIncident: play.api.libs.json.Reads[Incident] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").read[Long] and
         (__ \ "summary").read[String] and
         (__ \ "description").readNullable[String] and
         (__ \ "team").readNullable[Team] and
         (__ \ "severity").read[Incident.Severity] and
         (__ \ "tags").readNullable[scala.collection.Seq[String]].map { x =>
          x.getOrElse(Nil)
        } and
         (__ \ "plan").readNullable[Plan] and
         (__ \ "created_at").read[org.joda.time.DateTime])(Incident.apply _)
      }
    
    implicit def writesIncident: play.api.libs.json.Writes[Incident] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").write[Long] and
         (__ \ "summary").write[String] and
         (__ \ "description").write[scala.Option[String]] and
         (__ \ "team").write[scala.Option[Team]] and
         (__ \ "severity").write[Incident.Severity] and
         (__ \ "tags").write[scala.collection.Seq[String]] and
         (__ \ "plan").write[scala.Option[Plan]] and
         (__ \ "created_at").write[org.joda.time.DateTime])(unlift(Incident.unapply))
      }
    
    implicit def readsPlan: play.api.libs.json.Reads[Plan] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").read[Long] and
         (__ \ "incident_id").read[Long] and
         (__ \ "body").read[String] and
         (__ \ "grade").readNullable[Int] and
         (__ \ "created_at").read[org.joda.time.DateTime])(Plan.apply _)
      }
    
    implicit def writesPlan: play.api.libs.json.Writes[Plan] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").write[Long] and
         (__ \ "incident_id").write[Long] and
         (__ \ "body").write[String] and
         (__ \ "grade").write[scala.Option[Int]] and
         (__ \ "created_at").write[org.joda.time.DateTime])(unlift(Plan.unapply))
      }
    
    implicit def readsTeam: play.api.libs.json.Reads[Team] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        (__ \ "key").read[String].map { x =>
          new Team(key = x)
        }
      }
    
    implicit def writesTeam: play.api.libs.json.Writes[Team] =
      new play.api.libs.json.Writes[Team] {
        def writes(x: Team) = play.api.libs.json.Json.obj(
          "key" -> play.api.libs.json.Json.toJson(x.key)
        )
      }
  }
}

package quality {
  trait Response[T] {
    val entity: T
    val status: Int
  }

  object Response {
    def unapply[T](r: Response[T]) = Some((r.entity, r.status))
  }

  case class ResponseImpl[T](entity: T, status: Int) extends Response[T]

  case class FailedResponse[T](entity: T, status: Int)
    extends Exception(s"request failed with status[$status]: ${entity}")
    with Response[T]

  class Client(apiUrl: String, apiToken: Option[String] = None) {
    import quality.models._
    import quality.models.json._

    private val logger = play.api.Logger("quality.client")

    logger.info(s"Initializing quality.client for url $apiUrl")

    def requestHolder(path: String): play.api.libs.ws.WSRequestHolder = {
      import play.api.Play.current

      val url = apiUrl + path
      val holder = play.api.libs.ws.WS.url(url)
      apiToken match {
        case None => holder
        case Some(token: String) => {
          holder.withAuth(token, "", play.api.libs.ws.WSAuthScheme.BASIC)
        }
      }
    }

    def logRequest(method: String, req: play.api.libs.ws.WSRequestHolder)(implicit ec: scala.concurrent.ExecutionContext): play.api.libs.ws.WSRequestHolder = {
      val q = req.queryString.flatMap { case (name, values) =>
        values.map(name -> _).map { case (name, value) =>
          s"$name=$value"
        }
      }.mkString("&")
      val url = s"${req.url}?$q"
      apiToken.map { _ =>
        logger.info(s"curl -X $method -u '[REDACTED]:' $url")
      }.getOrElse {
        logger.info(s"curl -X $method $url")
      }
      req
    }

    def processResponse(f: scala.concurrent.Future[play.api.libs.ws.WSResponse])(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      f.map { response =>
        lazy val body: String = scala.util.Try {
          play.api.libs.json.Json.prettyPrint(response.json)
        } getOrElse {
          response.body
        }
        logger.debug(s"${response.status} -> $body")
        response
      }
    }

    private def POST(path: String, data: play.api.libs.json.JsValue)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      processResponse(logRequest("POST", requestHolder(path)).post(data))
    }

    private def GET(path: String, q: Seq[(String, String)])(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      processResponse(logRequest("GET", requestHolder(path).withQueryString(q:_*)).get())
    }

    private def PUT(path: String, data: play.api.libs.json.JsValue)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      processResponse(logRequest("PUT", requestHolder(path)).put(data))
    }

    private def PATCH(path: String, data: play.api.libs.json.JsValue)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      processResponse(logRequest("PATCH", requestHolder(path)).patch(data))
    }

    private def DELETE(path: String)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      processResponse(logRequest("DELETE", requestHolder(path)).delete())
    }

    object Events {
      /**
       * Search all events. Results are always paginated. Events are sorted in time order
       * - the first record is the most recent event.
       */
      def get(
        model: scala.Option[String] = None,
        action: scala.Option[String] = None,
        numberHours: scala.Option[Int] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Event]]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= model.map { x =>
          "model" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= action.map { x =>
          "action" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= numberHours.map { x =>
          "number_hours" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= limit.map { x =>
          "limit" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= offset.map { x =>
          "offset" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        
        GET(s"/events", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Event]], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
    }
    
    object Healthchecks {
      def get(
      
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Healthcheck]]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        
        
        GET(s"/_internal_/healthcheck", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Healthcheck]], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
    }
    
    object Incidents {
      /**
       * Search all incidents. Results are always paginated.
       */
      def get(
        id: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        hasPlan: scala.Option[Boolean] = None,
        hasGrade: scala.Option[Boolean] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Incident]]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= id.map { x =>
          "id" -> (
            { x: Long =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= teamKey.map { x =>
          "team_key" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= hasPlan.map { x =>
          "has_plan" -> (
            { x: Boolean =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= hasGrade.map { x =>
          "has_grade" -> (
            { x: Boolean =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= limit.map { x =>
          "limit" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= offset.map { x =>
          "offset" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        
        GET(s"/incidents", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Incident]], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Returns information about the incident with this specific id.
       */
      def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Incident]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        
        
        GET(s"/incidents/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[Incident], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Create a new incident.
       */
      def post(
        teamKey: scala.Option[String] = None,
        severity: String,
        summary: String,
        description: scala.Option[String] = None,
        tags: scala.collection.Seq[String] = Nil
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Incident]] = {
        val payload = play.api.libs.json.Json.obj(
          "team_key" -> play.api.libs.json.Json.toJson(teamKey),
          "severity" -> play.api.libs.json.Json.toJson(severity),
          "summary" -> play.api.libs.json.Json.toJson(summary),
          "description" -> play.api.libs.json.Json.toJson(description),
          "tags" -> play.api.libs.json.Json.toJson(tags)
        )
        
        POST(s"/incidents", payload).map {
          case r if r.status == 201 => new ResponseImpl(r.json.as[Incident], 201)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Updates an incident.
       */
      def putById(
        id: Long,
        teamKey: scala.Option[String] = None,
        severity: String,
        summary: String,
        description: scala.Option[String] = None,
        tags: scala.collection.Seq[String] = Nil
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Incident]] = {
        val payload = play.api.libs.json.Json.obj(
          "team_key" -> play.api.libs.json.Json.toJson(teamKey),
          "severity" -> play.api.libs.json.Json.toJson(severity),
          "summary" -> play.api.libs.json.Json.toJson(summary),
          "description" -> play.api.libs.json.Json.toJson(description),
          "tags" -> play.api.libs.json.Json.toJson(tags)
        )
        
        PUT(s"/incidents/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}", payload).map {
          case r if r.status == 201 => new ResponseImpl(r.json.as[Incident], 201)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Unit]] = {
        DELETE(s"/incidents/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}").map {
          case r if r.status == 204 => new ResponseImpl((), 204)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
    }
    
    object Plans {
      /**
       * Search all plans. Results are always paginated.
       */
      def get(
        id: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Plan]]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= id.map { x =>
          "id" -> (
            { x: Long =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= incidentId.map { x =>
          "incident_id" -> (
            { x: Long =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= teamKey.map { x =>
          "team_key" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= limit.map { x =>
          "limit" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= offset.map { x =>
          "offset" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        
        GET(s"/plans", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Plan]], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Create a plan.
       */
      def post(
        incidentId: Long,
        body: String,
        grade: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Plan]] = {
        val payload = play.api.libs.json.Json.obj(
          "incident_id" -> play.api.libs.json.Json.toJson(incidentId),
          "body" -> play.api.libs.json.Json.toJson(body),
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )
        
        POST(s"/plans", payload).map {
          case r if r.status == 201 => new ResponseImpl(r.json.as[Plan], 201)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Update a plan.
       */
      def putById(
        id: Long,
        incidentId: Long,
        body: String,
        grade: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Plan]] = {
        val payload = play.api.libs.json.Json.obj(
          "incident_id" -> play.api.libs.json.Json.toJson(incidentId),
          "body" -> play.api.libs.json.Json.toJson(body),
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )
        
        PUT(s"/plans/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}", payload).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[Plan], 200)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Update the grade assigned to a plan.
       */
      def putGradeById(
        id: Long,
        grade: Int
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Plan]] = {
        val payload = play.api.libs.json.Json.obj(
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )
        
        PUT(s"/plans/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}/grade", payload).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[Plan], 200)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Get a single plan.
       */
      def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Plan]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        
        
        GET(s"/plans/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[Plan], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Delete a plan.
       */
      def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Unit]] = {
        DELETE(s"/plans/${({x: Long =>
          val s = x.toString
          java.net.URLEncoder.encode(s, "UTF-8")
        })(id)}").map {
          case r if r.status == 204 => new ResponseImpl((), 204)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
    }
    
    object Teams {
      /**
       * Search all teams. Results are always paginated.
       */
      def get(
        key: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Team]]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= key.map { x =>
          "key" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= limit.map { x =>
          "limit" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        queryBuilder ++= offset.map { x =>
          "offset" -> (
            { x: Int =>
              x.toString
            }
          )(x)
        }
        
        GET(s"/teams", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Team]], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Returns information about the team with this specific key.
       */
      def getByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Team]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        
        
        GET(s"/teams/${({x: String =>
          val s = x
          java.net.URLEncoder.encode(s, "UTF-8")
        })(key)}", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[Team], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Create a new team.
       */
      def post(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Team]] = {
        val payload = play.api.libs.json.Json.obj(
          "key" -> play.api.libs.json.Json.toJson(key)
        )
        
        POST(s"/teams", payload).map {
          case r if r.status == 201 => new ResponseImpl(r.json.as[Team], 201)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      def deleteByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Unit]] = {
        DELETE(s"/teams/${({x: String =>
          val s = x
          java.net.URLEncoder.encode(s, "UTF-8")
        })(key)}").map {
          case r if r.status == 204 => new ResponseImpl((), 204)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
    }
  }
}
