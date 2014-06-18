package quality.models {
  case class Error(
    code: String,
    message: String
  )

  object Error {
    case class Patch(
      code: scala.Option[String] = None,
      message: scala.Option[String] = None
    ) {
    
      def code(value: String): Patch = copy(code = Option(value))
      
      def message(value: String): Patch = copy(message = Option(value))
    
      def apply(x: Error): Error = x.copy(
        code = code.getOrElse(x.code),
        message = message.getOrElse(x.message)
      )
    }
  }

  case class Grade(
    guid: java.util.UUID,
    report: Report,
    value: String
  )

  object Grade {
    case class Patch(
      guid: scala.Option[java.util.UUID] = None,
      report: scala.Option[Report] = None,
      value: scala.Option[String] = None
    ) {
    
      def guid(value: java.util.UUID): Patch = copy(guid = Option(value))
      
      def report(value: Report): Patch = copy(report = Option(value))
      
      def value(value: String): Patch = copy(value = Option(value))
    
      def apply(x: Grade): Grade = x.copy(
        guid = guid.getOrElse(x.guid),
        report = report.getOrElse(x.report),
        value = value.getOrElse(x.value)
      )
    }
  }

  case class Incident(
    guid: java.util.UUID,
    summary: String,
    description: String,
    teamKey: String,
    severity: String
  )

  object Incident {
    case class Patch(
      guid: scala.Option[java.util.UUID] = None,
      summary: scala.Option[String] = None,
      description: scala.Option[String] = None,
      teamKey: scala.Option[String] = None,
      severity: scala.Option[String] = None
    ) {
    
      def guid(value: java.util.UUID): Patch = copy(guid = Option(value))
      
      def summary(value: String): Patch = copy(summary = Option(value))
      
      def description(value: String): Patch = copy(description = Option(value))
      
      def teamKey(value: String): Patch = copy(teamKey = Option(value))
      
      def severity(value: String): Patch = copy(severity = Option(value))
    
      def apply(x: Incident): Incident = x.copy(
        guid = guid.getOrElse(x.guid),
        summary = summary.getOrElse(x.summary),
        description = description.getOrElse(x.description),
        teamKey = teamKey.getOrElse(x.teamKey),
        severity = severity.getOrElse(x.severity)
      )
    }
  }

  case class Report(
    guid: java.util.UUID,
    incident: Incident,
    body: String
  )

  object Report {
    case class Patch(
      guid: scala.Option[java.util.UUID] = None,
      incident: scala.Option[Incident] = None,
      body: scala.Option[String] = None
    ) {
    
      def guid(value: java.util.UUID): Patch = copy(guid = Option(value))
      
      def incident(value: Incident): Patch = copy(incident = Option(value))
      
      def body(value: String): Patch = copy(body = Option(value))
    
      def apply(x: Report): Report = x.copy(
        guid = guid.getOrElse(x.guid),
        incident = incident.getOrElse(x.incident),
        body = body.getOrElse(x.body)
      )
    }
  }

}

package quality.models {
  package object json {
    import play.api.libs.json._
    import play.api.libs.functional.syntax._

    implicit val readsUUID = __.read[String].map(java.util.UUID.fromString)

    implicit val writesUUID = new Writes[java.util.UUID] {
      def writes(x: java.util.UUID) = JsString(x.toString)
    }

    implicit val readsJodaDateTime = __.read[String].map { str =>
      import org.joda.time.format.ISODateTimeFormat.dateTimeParser
      dateTimeParser.parseDateTime(str)
    }

    implicit val writesJodaDateTime = new Writes[org.joda.time.DateTime] {
      def writes(x: org.joda.time.DateTime) = {
        import org.joda.time.format.ISODateTimeFormat.dateTime
        val str = dateTime.print(x)
        JsString(str)
      }
    }

    implicit def readsError: play.api.libs.json.Reads[Error] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "code").read[String] and
       (__ \ "message").read[String])(Error.apply _)
    }
    
    implicit def readsError_Patch: play.api.libs.json.Reads[Error.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "code").readNullable[String] and
       (__ \ "message").readNullable[String])(Error.Patch.apply _)
    }
    
    
    implicit def writesError: play.api.libs.json.Writes[Error] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "code").write[String] and
       (__ \ "message").write[String])(unlift(Error.unapply))
    }
    
    implicit def writesError_Patch: play.api.libs.json.Writes[Error.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "code").writeNullable[String] and
       (__ \ "message").writeNullable[String])(unlift(Error.Patch.unapply))
    }
    
    
    implicit def readsGrade: play.api.libs.json.Reads[Grade] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").read[java.util.UUID] and
       (__ \ "report").lazyRead(readsReport) and
       (__ \ "value").read[String])(Grade.apply _)
    }
    
    implicit def readsGrade_Patch: play.api.libs.json.Reads[Grade.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").readNullable[java.util.UUID] and
       (__ \ "report").lazyReadNullable(readsReport) and
       (__ \ "value").readNullable[String])(Grade.Patch.apply _)
    }
    
    
    implicit def writesGrade: play.api.libs.json.Writes[Grade] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").write[java.util.UUID] and
       (__ \ "report").lazyWrite(writesReport) and
       (__ \ "value").write[String])(unlift(Grade.unapply))
    }
    
    implicit def writesGrade_Patch: play.api.libs.json.Writes[Grade.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").writeNullable[java.util.UUID] and
       (__ \ "report").lazyWriteNullable(writesReport) and
       (__ \ "value").writeNullable[String])(unlift(Grade.Patch.unapply))
    }
    
    
    implicit def readsIncident: play.api.libs.json.Reads[Incident] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").read[java.util.UUID] and
       (__ \ "summary").read[String] and
       (__ \ "description").read[String] and
       (__ \ "team_key").read[String] and
       (__ \ "severity").read[String])(Incident.apply _)
    }
    
    implicit def readsIncident_Patch: play.api.libs.json.Reads[Incident.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").readNullable[java.util.UUID] and
       (__ \ "summary").readNullable[String] and
       (__ \ "description").readNullable[String] and
       (__ \ "team_key").readNullable[String] and
       (__ \ "severity").readNullable[String])(Incident.Patch.apply _)
    }
    
    
    implicit def writesIncident: play.api.libs.json.Writes[Incident] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").write[java.util.UUID] and
       (__ \ "summary").write[String] and
       (__ \ "description").write[String] and
       (__ \ "team_key").write[String] and
       (__ \ "severity").write[String])(unlift(Incident.unapply))
    }
    
    implicit def writesIncident_Patch: play.api.libs.json.Writes[Incident.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").writeNullable[java.util.UUID] and
       (__ \ "summary").writeNullable[String] and
       (__ \ "description").writeNullable[String] and
       (__ \ "team_key").writeNullable[String] and
       (__ \ "severity").writeNullable[String])(unlift(Incident.Patch.unapply))
    }
    
    
    implicit def readsReport: play.api.libs.json.Reads[Report] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").read[java.util.UUID] and
       (__ \ "incident").lazyRead(readsIncident) and
       (__ \ "body").read[String])(Report.apply _)
    }
    
    implicit def readsReport_Patch: play.api.libs.json.Reads[Report.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").readNullable[java.util.UUID] and
       (__ \ "incident").lazyReadNullable(readsIncident) and
       (__ \ "body").readNullable[String])(Report.Patch.apply _)
    }
    
    
    implicit def writesReport: play.api.libs.json.Writes[Report] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").write[java.util.UUID] and
       (__ \ "incident").lazyWrite(writesIncident) and
       (__ \ "body").write[String])(unlift(Report.unapply))
    }
    
    implicit def writesReport_Patch: play.api.libs.json.Writes[Report.Patch] = {
      import play.api.libs.json._
      import play.api.libs.functional.syntax._
      ((__ \ "guid").writeNullable[java.util.UUID] and
       (__ \ "incident").lazyWriteNullable(writesIncident) and
       (__ \ "body").writeNullable[String])(unlift(Report.Patch.unapply))
    }
  }
}

package quality {
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

    object Incidents {
      /**
       * Search all incidents. Results are always paginated.
       */
      def get(
        guid: scala.Option[java.util.UUID] = None,
        teamKey: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[scala.collection.Seq[Incident]]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= guid.map { x =>
          "guid" -> (
            { x: java.util.UUID =>
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
        val query = queryBuilder.result
        processResponse(logRequest("GET", requestHolder(s"/incidents")
          .withQueryString(query:_*)).get()).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[scala.collection.Seq[Incident]], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Returns information about the incident with this specific guid.
       */
      def getByGuid(
        guid: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Incident]] = {
        val queryBuilder = List.newBuilder[(String, String)]
        
        val query = queryBuilder.result
        processResponse(logRequest("GET", requestHolder(s"/incidents/${({x: String =>
          val s = x
          java.net.URLEncoder.encode(s, "UTF-8")
        })(guid)}")
          .withQueryString(query:_*)).get()).map {
          case r if r.status == 200 => new ResponseImpl(r.json.as[Incident], 200)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Create a new incident.
       */
      def post(
        summary: String,
        description: String,
        teamKey: String,
        severity: String,
        _body: Incident
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Incident]] = {
        val payload = play.api.libs.json.Json.toJson(_body)
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= Seq(summary).map { x =>
          "summary" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= Seq(description).map { x =>
          "description" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= Seq(teamKey).map { x =>
          "team_key" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= Seq(severity).map { x =>
          "severity" -> (
            { x: String =>
              x
            }
          )(x)
        }
        val query = queryBuilder.result
        processResponse(logRequest("POST", requestHolder(s"/incidents"))
          .withQueryString(query:_*).post(payload)).map {
          case r if r.status == 201 => new ResponseImpl(r.json.as[Incident], 201)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
      
      /**
       * Updates information about the incident with the specified guid.
       */
      def putByGuid(
        guid: String,
        summary: String,
        description: String,
        teamKey: String,
        severity: String,
        _body: Incident
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[Response[Incident]] = {
        val payload = play.api.libs.json.Json.toJson(_body)
        val queryBuilder = List.newBuilder[(String, String)]
        queryBuilder ++= Seq(summary).map { x =>
          "summary" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= Seq(description).map { x =>
          "description" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= Seq(teamKey).map { x =>
          "team_key" -> (
            { x: String =>
              x
            }
          )(x)
        }
        queryBuilder ++= Seq(severity).map { x =>
          "severity" -> (
            { x: String =>
              x
            }
          )(x)
        }
        val query = queryBuilder.result
        processResponse(logRequest("PUT", requestHolder(s"/incidents/${({x: String =>
          val s = x
          java.net.URLEncoder.encode(s, "UTF-8")
        })(guid)}"))
          .withQueryString(query:_*).put(payload)).map {
          case r if r.status == 201 => new ResponseImpl(r.json.as[Incident], 201)
          case r if r.status == 409 => throw new FailedResponse(r.json.as[scala.collection.Seq[Error]], 409)
          case r => throw new FailedResponse(r.body, r.status)
        }
      }
    }
  }
}
