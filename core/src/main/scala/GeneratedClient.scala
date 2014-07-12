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
  case class Statistic(
    team: Team,
    totalGrades: Long,
    averageGrade: scala.Option[Int] = None,
    totalOpenIncidents: Long,
    totalIncidents: Long,
    totalPlans: Long,
    plans: scala.collection.Seq[Plan] = Nil
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
    
    implicit def readsStatistic: play.api.libs.json.Reads[Statistic] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "team").read[Team] and
         (__ \ "total_grades").read[Long] and
         (__ \ "average_grade").readNullable[Int] and
         (__ \ "total_open_incidents").read[Long] and
         (__ \ "total_incidents").read[Long] and
         (__ \ "total_plans").read[Long] and
         (__ \ "plans").readNullable[scala.collection.Seq[Plan]].map { x =>
          x.getOrElse(Nil)
        })(Statistic.apply _)
      }
    
    implicit def writesStatistic: play.api.libs.json.Writes[Statistic] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "team").write[Team] and
         (__ \ "total_grades").write[Long] and
         (__ \ "average_grade").write[scala.Option[Int]] and
         (__ \ "total_open_incidents").write[Long] and
         (__ \ "total_incidents").write[Long] and
         (__ \ "total_plans").write[Long] and
         (__ \ "plans").write[scala.collection.Seq[Plan]])(unlift(Statistic.unapply))
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

  case class FailedResponse(response: play.api.libs.ws.Response) extends Exception

  package error {
  
    import quality.models.json._
  
    case class ErrorsResponse(response: play.api.libs.ws.Response) extends Exception {
    
      lazy val errors = response.json.as[scala.collection.Seq[quality.models.Error]]
    
    }
  }


  class Client(apiUrl: String, apiToken: scala.Option[String] = None) {
    import quality.models._
    import quality.models.json._

    private val logger = play.api.Logger("quality.client")

    logger.info(s"Initializing quality.client for url $apiUrl")

    def events = Events
    
    def healthchecks = Healthchecks
    
    def incidents = Incidents
    
    def plans = Plans
    
    def statistics = Statistics
    
    def teams = Teams

    def _requestHolder(path: String): play.api.libs.ws.WSRequestHolder = {
      import play.api.Play.current

      val holder = play.api.libs.ws.WS.url(apiUrl + path)
      apiToken.fold(holder) { token =>
        holder.withAuth(token, "", play.api.libs.ws.WSAuthScheme.BASIC)
      }
    }

    def _logRequest(method: String, req: play.api.libs.ws.WSRequestHolder)(implicit ec: scala.concurrent.ExecutionContext): play.api.libs.ws.WSRequestHolder = {
      val queryComponents = for {
        (name, values) <- req.queryString
        value <- values
      } yield name -> value
      val url = s"${req.url}${queryComponents.mkString("?", "&", "")}"
      apiToken.fold(logger.info(s"curl -X $method $url")) { _ =>
        logger.info(s"curl -X $method -u '[REDACTED]:' $url")
      }
      req
    }

    private def POST(path: String, data: play.api.libs.json.JsValue = play.api.libs.json.Json.obj())(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      _logRequest("POST", _requestHolder(path)).post(data)
    }

    private def GET(path: String, q: Seq[(String, String)] = Seq.empty)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      _logRequest("GET", _requestHolder(path).withQueryString(q:_*)).get()
    }

    private def PUT(path: String, data: play.api.libs.json.JsValue = play.api.libs.json.Json.obj())(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      _logRequest("PUT", _requestHolder(path)).put(data)
    }

    private def PATCH(path: String, data: play.api.libs.json.JsValue = play.api.libs.json.Json.obj())(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      _logRequest("PATCH", _requestHolder(path)).patch(data)
    }

    private def DELETE(path: String)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      _logRequest("DELETE", _requestHolder(path)).delete()
    }

    trait Events {
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
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Event]]
    }
  
    object Events extends Events {
      override def get(
        model: scala.Option[String] = None,
        action: scala.Option[String] = None,
        numberHours: scala.Option[Int] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Event]] = {
        val query = Seq(
          model.map("model" -> _),
          action.map("action" -> _),
          numberHours.map("number_hours" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten
        
        GET(s"/events", query).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[quality.models.Event]]
          case r => throw new FailedResponse(r)
        }
      }
    }
  
    trait Healthchecks {
      def get()(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Healthcheck]]
    }
  
    object Healthchecks extends Healthchecks {
      override def get()(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Healthcheck]] = {
        GET(s"/_internal_/healthcheck").map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[quality.models.Healthcheck]]
          case r => throw new FailedResponse(r)
        }
      }
    }
  
    trait Incidents {
      /**
       * Search all incidents. Results are always paginated.
       */
      def get(
        id: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        hasTeam: scala.Option[Boolean] = None,
        hasPlan: scala.Option[Boolean] = None,
        hasGrade: scala.Option[Boolean] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Incident]]
      
      /**
       * Returns information about the incident with this specific id.
       */
      def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[quality.models.Incident]]
      
      /**
       * Create a new incident.
       */
      def post(
        teamKey: scala.Option[String] = None,
        severity: String,
        summary: String,
        description: scala.Option[String] = None,
        tags: scala.collection.Seq[String] = Nil
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Incident]
      
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
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Incident]
      
      def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[Unit]]
    }
  
    object Incidents extends Incidents {
      override def get(
        id: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        hasTeam: scala.Option[Boolean] = None,
        hasPlan: scala.Option[Boolean] = None,
        hasGrade: scala.Option[Boolean] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Incident]] = {
        val query = Seq(
          id.map("id" -> _.toString),
          teamKey.map("team_key" -> _),
          hasTeam.map("has_team" -> _.toString),
          hasPlan.map("has_plan" -> _.toString),
          hasGrade.map("has_grade" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten
        
        GET(s"/incidents", query).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[quality.models.Incident]]
          case r => throw new FailedResponse(r)
        }
      }
      
      override def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[quality.models.Incident]] = {
        GET(s"/incidents/${id}").map {
          case r if r.status == 200 => Some(r.json.as[quality.models.Incident])
          case r if r.status == 404 => None
          case r => throw new FailedResponse(r)
        }
      }
      
      override def post(
        teamKey: scala.Option[String] = None,
        severity: String,
        summary: String,
        description: scala.Option[String] = None,
        tags: scala.collection.Seq[String] = Nil
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Incident] = {
        val payload = play.api.libs.json.Json.obj(
          "team_key" -> play.api.libs.json.Json.toJson(teamKey),
          "severity" -> play.api.libs.json.Json.toJson(severity),
          "summary" -> play.api.libs.json.Json.toJson(summary),
          "description" -> play.api.libs.json.Json.toJson(description),
          "tags" -> play.api.libs.json.Json.toJson(tags)
        )
        
        POST(s"/incidents", payload).map {
          case r if r.status == 201 => r.json.as[quality.models.Incident]
          case r if r.status == 409 => throw new quality.error.ErrorsResponse(r)
          case r => throw new FailedResponse(r)
        }
      }
      
      override def putById(
        id: Long,
        teamKey: scala.Option[String] = None,
        severity: String,
        summary: String,
        description: scala.Option[String] = None,
        tags: scala.collection.Seq[String] = Nil
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Incident] = {
        val payload = play.api.libs.json.Json.obj(
          "team_key" -> play.api.libs.json.Json.toJson(teamKey),
          "severity" -> play.api.libs.json.Json.toJson(severity),
          "summary" -> play.api.libs.json.Json.toJson(summary),
          "description" -> play.api.libs.json.Json.toJson(description),
          "tags" -> play.api.libs.json.Json.toJson(tags)
        )
        
        PUT(s"/incidents/${id}", payload).map {
          case r if r.status == 201 => r.json.as[quality.models.Incident]
          case r if r.status == 409 => throw new quality.error.ErrorsResponse(r)
          case r => throw new FailedResponse(r)
        }
      }
      
      override def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[Unit]] = {
        DELETE(s"/incidents/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedResponse(r)
        }
      }
    }
  
    trait Plans {
      /**
       * Search all plans. Results are always paginated.
       */
      def get(
        id: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Plan]]
      
      /**
       * Create a plan.
       */
      def post(
        incidentId: Long,
        body: String,
        grade: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Plan]
      
      /**
       * Update a plan.
       */
      def putById(
        id: Long,
        incidentId: Long,
        body: String,
        grade: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Plan]
      
      /**
       * Update the grade assigned to a plan.
       */
      def putGradeById(
        id: Long,
        grade: Int
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Plan]
      
      /**
       * Get a single plan.
       */
      def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[quality.models.Plan]]
      
      /**
       * Delete a plan.
       */
      def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[Unit]]
    }
  
    object Plans extends Plans {
      override def get(
        id: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Plan]] = {
        val query = Seq(
          id.map("id" -> _.toString),
          incidentId.map("incident_id" -> _.toString),
          teamKey.map("team_key" -> _),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten
        
        GET(s"/plans", query).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[quality.models.Plan]]
          case r => throw new FailedResponse(r)
        }
      }
      
      override def post(
        incidentId: Long,
        body: String,
        grade: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Plan] = {
        val payload = play.api.libs.json.Json.obj(
          "incident_id" -> play.api.libs.json.Json.toJson(incidentId),
          "body" -> play.api.libs.json.Json.toJson(body),
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )
        
        POST(s"/plans", payload).map {
          case r if r.status == 201 => r.json.as[quality.models.Plan]
          case r if r.status == 409 => throw new quality.error.ErrorsResponse(r)
          case r => throw new FailedResponse(r)
        }
      }
      
      override def putById(
        id: Long,
        incidentId: Long,
        body: String,
        grade: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Plan] = {
        val payload = play.api.libs.json.Json.obj(
          "incident_id" -> play.api.libs.json.Json.toJson(incidentId),
          "body" -> play.api.libs.json.Json.toJson(body),
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )
        
        PUT(s"/plans/${id}", payload).map {
          case r if r.status == 200 => r.json.as[quality.models.Plan]
          case r if r.status == 409 => throw new quality.error.ErrorsResponse(r)
          case r => throw new FailedResponse(r)
        }
      }
      
      override def putGradeById(
        id: Long,
        grade: Int
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Plan] = {
        val payload = play.api.libs.json.Json.obj(
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )
        
        PUT(s"/plans/${id}/grade", payload).map {
          case r if r.status == 200 => r.json.as[quality.models.Plan]
          case r if r.status == 409 => throw new quality.error.ErrorsResponse(r)
          case r => throw new FailedResponse(r)
        }
      }
      
      override def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[quality.models.Plan]] = {
        GET(s"/plans/${id}").map {
          case r if r.status == 200 => Some(r.json.as[quality.models.Plan])
          case r if r.status == 404 => None
          case r => throw new FailedResponse(r)
        }
      }
      
      override def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[Unit]] = {
        DELETE(s"/plans/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedResponse(r)
        }
      }
    }
  
    trait Statistics {
      /**
       * Retrieve team statistics for all or one team.
       */
      def get(
        teamKey: scala.Option[String] = None,
        numberHours: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Statistic]]
    }
  
    object Statistics extends Statistics {
      override def get(
        teamKey: scala.Option[String] = None,
        numberHours: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Statistic]] = {
        val query = Seq(
          teamKey.map("team_key" -> _),
          numberHours.map("number_hours" -> _.toString)
        ).flatten
        
        GET(s"/statistics", query).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[quality.models.Statistic]]
          case r => throw new FailedResponse(r)
        }
      }
    }
  
    trait Teams {
      /**
       * Search all teams. Results are always paginated.
       */
      def get(
        key: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Team]]
      
      /**
       * Returns information about the team with this specific key.
       */
      def getByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[quality.models.Team]]
      
      /**
       * Create a new team.
       */
      def post(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Team]
      
      def deleteByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[Unit]]
    }
  
    object Teams extends Teams {
      override def get(
        key: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[quality.models.Team]] = {
        val query = Seq(
          key.map("key" -> _),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten
        
        GET(s"/teams", query).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[quality.models.Team]]
          case r => throw new FailedResponse(r)
        }
      }
      
      override def getByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[quality.models.Team]] = {
        GET(s"/teams/${java.net.URLEncoder.encode(key, "UTF-8")}").map {
          case r if r.status == 200 => Some(r.json.as[quality.models.Team])
          case r if r.status == 404 => None
          case r => throw new FailedResponse(r)
        }
      }
      
      override def post(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[quality.models.Team] = {
        val payload = play.api.libs.json.Json.obj(
          "key" -> play.api.libs.json.Json.toJson(key)
        )
        
        POST(s"/teams", payload).map {
          case r if r.status == 201 => r.json.as[quality.models.Team]
          case r if r.status == 409 => throw new quality.error.ErrorsResponse(r)
          case r => throw new FailedResponse(r)
        }
      }
      
      override def deleteByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Option[Unit]] = {
        DELETE(s"/teams/${java.net.URLEncoder.encode(key, "UTF-8")}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedResponse(r)
        }
      }
    }
  }
}
