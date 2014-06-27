package quality.models {
  case class Error(
    code: String,
    message: String
  )
  case class Grade(
    id: Long,
    report: Report,
    value: String
  )
  case class Healthcheck(
    status: String
  )
  case class Incident(
    id: Long,
    summary: String,
    description: scala.Option[String] = None,
    teamKey: String,
    severity: Incident.Severity,
    tags: scala.collection.Seq[String] = Nil
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
  case class Report(
    id: Long,
    incident: Incident,
    body: String
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
    
    implicit def readsGrade: play.api.libs.json.Reads[Grade] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").read[Long] and
         (__ \ "report").read[Report] and
         (__ \ "value").read[String])(Grade.apply _)
      }
    
    implicit def writesGrade: play.api.libs.json.Writes[Grade] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").write[Long] and
         (__ \ "report").write[Report] and
         (__ \ "value").write[String])(unlift(Grade.unapply))
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
         (__ \ "team_key").read[String] and
         (__ \ "severity").read[Incident.Severity] and
         (__ \ "tags").readNullable[scala.collection.Seq[String]].map { x =>
          x.getOrElse(Nil)
        })(Incident.apply _)
      }
    
    implicit def writesIncident: play.api.libs.json.Writes[Incident] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").write[Long] and
         (__ \ "summary").write[String] and
         (__ \ "description").write[scala.Option[String]] and
         (__ \ "team_key").write[String] and
         (__ \ "severity").write[Incident.Severity] and
         (__ \ "tags").write[scala.collection.Seq[String]])(unlift(Incident.unapply))
      }
    
    implicit def readsReport: play.api.libs.json.Reads[Report] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").read[Long] and
         (__ \ "incident").read[Incident] and
         (__ \ "body").read[String])(Report.apply _)
      }
    
    implicit def writesReport: play.api.libs.json.Writes[Report] =
      {
        import play.api.libs.json._
        import play.api.libs.functional.syntax._
        ((__ \ "id").write[Long] and
         (__ \ "incident").write[Incident] and
         (__ \ "body").write[String])(unlift(Report.unapply))
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

    private def requestHolder(path: String) = {
      import play.api.Play.current

      val url = apiUrl + path
      val holder = play.api.libs.ws.WS.url(url)
      apiToken.map { token =>
        holder.withAuth(token, "", play.api.libs.ws.WSAuthScheme.BASIC)
      }.getOrElse {
        holder
      }
    }

    private def logRequest(method: String, req: play.api.libs.ws.WSRequestHolder)(implicit ec: scala.concurrent.ExecutionContext): play.api.libs.ws.WSRequestHolder = {
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

    private def processResponse(f: scala.concurrent.Future[play.api.libs.ws.WSResponse])(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
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
        teamKey: String,
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
        teamKey: String,
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
    
    object Reports {
      /**
       * Search all reports. Results are always paginated.
       */
      def get(
        id: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Report]]] = {
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
        
        GET(s"/reports", queryBuilder.result).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Report]], 200)
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