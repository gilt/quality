package com.gilt.quality.models {
  case class AdjournForm(
    adjournedAt: scala.Option[_root_.org.joda.time.DateTime] = None
  )

  /**
   * Describe an agenda item for a meeting. Currently the only agenda items we have
   * are that a particular incident needs to be reviewed.
   */
  case class AgendaItem(
    id: Long,
    meeting: com.gilt.quality.models.Meeting,
    incident: com.gilt.quality.models.Incident,
    task: com.gilt.quality.models.Task
  )

  case class AgendaItemForm(
    meetingId: Long,
    incidentId: Long,
    task: com.gilt.quality.models.Task
  )

  case class AuthenticationForm(
    email: String
  )

  case class EmailMessage(
    subject: String,
    body: String
  )

  case class Error(
    code: String,
    message: String
  )

<<<<<<< HEAD
=======
  /**
   * Represents something that has happened - e.g. a team was created, an incident
   * created, a plan updated, etc.
   */
  case class Event(
    model: com.gilt.quality.models.Model,
    action: com.gilt.quality.models.Action,
    timestamp: _root_.org.joda.time.DateTime,
    url: scala.Option[String] = None,
    data: com.gilt.quality.models.EventData
  )

  /**
   * Generic, descriptive data about a specific event
   */
  case class EventData(
    modelId: Long,
    summary: String
  )

  /**
   * Stores metadata about external services that can be integrated with the quality
   * app
   */
  case class ExternalService(
    id: Long,
    organization: com.gilt.quality.models.Organization,
    name: com.gilt.quality.models.ExternalServiceName,
    url: String,
    username: String
  )

  case class ExternalServiceForm(
    name: com.gilt.quality.models.ExternalServiceName,
    url: String,
    username: String,
    password: String
  )

>>>>>>> Add external service to capture information needed for JIRA integration
  case class Healthcheck(
    status: String
  )

  /**
   * URLs to key icons used through the application
   */
  case class Icons(
    smileyUrl: String,
    frownyUrl: String
  )

  /**
   * A bug or error that affected public or internal users in a negative way
   */
  case class Incident(
    id: Long,
    organization: com.gilt.quality.models.Organization,
    summary: String,
    description: scala.Option[String] = None,
    team: scala.Option[com.gilt.quality.models.Team] = None,
    severity: com.gilt.quality.models.Severity,
    tags: scala.collection.Seq[String] = Nil,
    plan: scala.Option[com.gilt.quality.models.Plan] = None,
    createdAt: _root_.org.joda.time.DateTime
  )

  case class IncidentForm(
    teamKey: scala.Option[String] = None,
    severity: com.gilt.quality.models.Severity,
    summary: String,
    description: scala.Option[String] = None,
    tags: scala.collection.Seq[String] = Nil
  )

  case class IncidentSummary(
    id: Long,
    severity: com.gilt.quality.models.Severity,
    summary: String
  )

  /**
   * Meetings happen on a regular schedule (e.g. thursdays from 11-12 EST). As
   * incidents are created, they are automatically assigned to the next meeting.
   * Incidents can then be reviewed from the context of a meeting, facilitating
   * online navigation. Incidents within a meeting can require one of two actions -
   * team assignment or plan review.
   */
  case class Meeting(
    id: Long,
    organization: com.gilt.quality.models.Organization,
    scheduledAt: _root_.org.joda.time.DateTime,
    adjournedAt: scala.Option[_root_.org.joda.time.DateTime] = None
  )

  case class MeetingForm(
    scheduledAt: _root_.org.joda.time.DateTime
  )

  /**
   * Used to enable pagination when walking through the issues in a particular
   * meeting. General idea is given a meeting id and an incident id, returns the
   * previous and next incident IDs, if any
   */
  case class MeetingPager(
    meeting: com.gilt.quality.models.Meeting,
    priorIncident: scala.Option[com.gilt.quality.models.Incident] = None,
    nextIncident: scala.Option[com.gilt.quality.models.Incident] = None
  )

  /**
   * Top level organization for which we are managing quality. Key entities like
   * teams and meetings are scoped to the organization.
   */
  case class Organization(
    key: String,
    name: String
  )

  case class OrganizationForm(
    name: String,
    key: scala.Option[String] = None
  )

  /**
   * Details for how an incident will be resolved
   */
  case class Plan(
    id: Long,
    incidentId: Long,
    body: String,
    grade: scala.Option[Int] = None,
    createdAt: _root_.org.joda.time.DateTime
  )

  case class PlanForm(
    incidentId: Long,
    body: String
  )

  /**
   * Statistics on each team's quality metrics, number of issues
   */
  case class Statistic(
    team: com.gilt.quality.models.Team,
    totalGrades: Long,
    averageGrade: scala.Option[Int] = None,
    totalOpenIncidents: Long,
    totalIncidents: Long,
    totalPlans: Long,
    plans: scala.collection.Seq[com.gilt.quality.models.Plan] = Nil
  )

  /**
   * Represents a user that is currently subscribed to a publication
   */
  case class Subscription(
    id: Long,
    organization: com.gilt.quality.models.Organization,
    user: com.gilt.quality.models.User,
    publication: com.gilt.quality.models.Publication
  )

  case class SubscriptionForm(
    organizationKey: String,
    userGuid: java.util.UUID,
    publication: com.gilt.quality.models.Publication
  )

  /**
   * A team is the main actor in the system. Teams have a unique key and own
   * incidents
   */
  case class Team(
    organization: com.gilt.quality.models.Organization,
    key: String,
    email: scala.Option[String] = None,
    icons: com.gilt.quality.models.Icons
  )

  case class TeamForm(
    key: String,
    email: scala.Option[String] = None,
    smileyUrl: scala.Option[String] = None,
    frownyUrl: scala.Option[String] = None
  )

  case class TeamMember(
    team: com.gilt.quality.models.Team,
    user: com.gilt.quality.models.User
  )

  /**
   * Statistics on each team's quality metrics, number of issues
   */
  case class TeamMemberSummary(
    team: com.gilt.quality.models.Team,
    numberMembers: Long
  )

  case class UpdateTeamForm(
    email: scala.Option[String] = None,
    smileyUrl: scala.Option[String] = None,
    frownyUrl: scala.Option[String] = None
  )

  /**
   * A user is a top level person.
   */
  case class User(
    guid: java.util.UUID,
    email: String
  )

  case class UserForm(
    email: String
  )

  /**
<<<<<<< HEAD
=======
   * Used in the event system to indicate what happened.
   */
  sealed trait Action

  object Action {

    /**
     * Indicates that an instance of this model was created
     */
    case object Created extends Action { override def toString = "created" }
    /**
     * Indicates that an instance of this model was updated
     */
    case object Updated extends Action { override def toString = "updated" }
    /**
     * Indicates that an instance of this model was deleted
     */
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

    def fromString(value: String): scala.Option[Action] = byName.get(value)

  }

  /**
   * An external service with which an organization can integrate.
   */
  sealed trait ExternalServiceName

  object ExternalServiceName {

    /**
     * Atlassian JIRA. If integration is enabled, an incident can be created by listing
     * the jira ISSUE number directly.
     */
    case object Jira extends ExternalServiceName { override def toString = "jira" }

    /**
     * UNDEFINED captures values that are sent either in error or
     * that were added by the server after this library was
     * generated. We want to make it easy and obvious for users of
     * this library to handle this case gracefully.
     *
     * We use all CAPS for the variable name to avoid collisions
     * with the camel cased values above.
     */
    case class UNDEFINED(override val toString: String) extends ExternalServiceName

    /**
     * all returns a list of all the valid, known values. We use
     * lower case to avoid collisions with the camel cased values
     * above.
     */
    val all = Seq(Jira)

    private[this]
    val byName = all.map(x => x.toString -> x).toMap

    def apply(value: String): ExternalServiceName = fromString(value).getOrElse(UNDEFINED(value))

    def fromString(value: String): scala.Option[ExternalServiceName] = byName.get(value)

  }

  /**
   * The name of the model that was the subject of the event
   */
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

    def fromString(value: String): scala.Option[Model] = byName.get(value)

  }

  /**
>>>>>>> Add external service to capture information needed for JIRA integration
   * A publication represents something that a user can subscribe to. An example
   * would be subscribing via email to the publication of all new incidents.
   */
  sealed trait Publication

  object Publication {

    /**
     * Email notification whenever an incident is created.
     */
    case object IncidentsCreate extends Publication { override def toString = "incidents.create" }
    /**
     * Email notification whenever an incident is updated.
     */
    case object IncidentsUpdate extends Publication { override def toString = "incidents.update" }
    /**
     * Email notification whenever a plan is created.
     */
    case object PlansCreate extends Publication { override def toString = "plans.create" }
    /**
     * Email notification whenever a plan is updated.
     */
    case object PlansUpdate extends Publication { override def toString = "plans.update" }
    /**
     * Email notification whenever a meeting is adjourned.
     */
    case object MeetingsAdjourned extends Publication { override def toString = "meetings.adjourned" }
    /**
     * Email notification whenever a team that you are on is assigned to an incident.
     */
    case object IncidentsTeamUpdate extends Publication { override def toString = "incidents.team_update" }

    /**
     * UNDEFINED captures values that are sent either in error or
     * that were added by the server after this library was
     * generated. We want to make it easy and obvious for users of
     * this library to handle this case gracefully.
     *
     * We use all CAPS for the variable name to avoid collisions
     * with the camel cased values above.
     */
    case class UNDEFINED(override val toString: String) extends Publication

    /**
     * all returns a list of all the valid, known values. We use
     * lower case to avoid collisions with the camel cased values
     * above.
     */
    val all = Seq(IncidentsCreate, IncidentsUpdate, PlansCreate, PlansUpdate, MeetingsAdjourned, IncidentsTeamUpdate)

    private[this]
    val byName = all.map(x => x.toString -> x).toMap

    def apply(value: String): Publication = fromString(value).getOrElse(UNDEFINED(value))

    def fromString(value: String): scala.Option[Publication] = byName.get(value)

  }

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

    def fromString(value: String): scala.Option[Severity] = byName.get(value)

  }

  /**
   * Describes what needs to be reviewed about a specific incident
   */
  sealed trait Task

  object Task {

    /**
     * Used in meetings to indicate that a team should be assigned to the incident or
     * that the group should review the team assignment to make sure it is correct.
     */
    case object ReviewTeam extends Task { override def toString = "review_team" }
    /**
     * Indicates that the incident plan should be reviewed and rated
     */
    case object ReviewPlan extends Task { override def toString = "review_plan" }

    /**
     * UNDEFINED captures values that are sent either in error or
     * that were added by the server after this library was
     * generated. We want to make it easy and obvious for users of
     * this library to handle this case gracefully.
     *
     * We use all CAPS for the variable name to avoid collisions
     * with the camel cased values above.
     */
    case class UNDEFINED(override val toString: String) extends Task

    /**
     * all returns a list of all the valid, known values. We use
     * lower case to avoid collisions with the camel cased values
     * above.
     */
    val all = Seq(ReviewTeam, ReviewPlan)

    private[this]
    val byName = all.map(x => x.toString -> x).toMap

    def apply(value: String): Task = fromString(value).getOrElse(UNDEFINED(value))

    def fromString(value: String): scala.Option[Task] = byName.get(value)

  }
}

package com.gilt.quality.models {
  package object json {
    import play.api.libs.json.__
    import play.api.libs.json.JsString
    import play.api.libs.json.Writes
    import play.api.libs.functional.syntax._

    private[quality] implicit val jsonReadsUUID = __.read[String].map(java.util.UUID.fromString)

    private[quality] implicit val jsonWritesUUID = new Writes[java.util.UUID] {
      def writes(x: java.util.UUID) = JsString(x.toString)
    }

    private[quality] implicit val jsonReadsJodaDateTime = __.read[String].map { str =>
      import org.joda.time.format.ISODateTimeFormat.dateTimeParser
      dateTimeParser.parseDateTime(str)
    }

    private[quality] implicit val jsonWritesJodaDateTime = new Writes[org.joda.time.DateTime] {
      def writes(x: org.joda.time.DateTime) = {
        import org.joda.time.format.ISODateTimeFormat.dateTime
        val str = dateTime.print(x)
        JsString(str)
      }
    }

<<<<<<< HEAD
=======
    implicit val jsonReadsQualityEnum_Action = __.read[String].map(Action.apply)
    implicit val jsonWritesQualityEnum_Action = new Writes[Action] {
      def writes(x: Action) = JsString(x.toString)
    }

    implicit val jsonReadsQualityEnum_ExternalServiceName = __.read[String].map(ExternalServiceName.apply)
    implicit val jsonWritesQualityEnum_ExternalServiceName = new Writes[ExternalServiceName] {
      def writes(x: ExternalServiceName) = JsString(x.toString)
    }

    implicit val jsonReadsQualityEnum_Model = __.read[String].map(Model.apply)
    implicit val jsonWritesQualityEnum_Model = new Writes[Model] {
      def writes(x: Model) = JsString(x.toString)
    }

>>>>>>> Add external service to capture information needed for JIRA integration
    implicit val jsonReadsQualityEnum_Publication = __.read[String].map(Publication.apply)
    implicit val jsonWritesQualityEnum_Publication = new Writes[Publication] {
      def writes(x: Publication) = JsString(x.toString)
    }

    implicit val jsonReadsQualityEnum_Severity = __.read[String].map(Severity.apply)
    implicit val jsonWritesQualityEnum_Severity = new Writes[Severity] {
      def writes(x: Severity) = JsString(x.toString)
    }

    implicit val jsonReadsQualityEnum_Task = __.read[String].map(Task.apply)
    implicit val jsonWritesQualityEnum_Task = new Writes[Task] {
      def writes(x: Task) = JsString(x.toString)
    }
    implicit def jsonReadsQualityAdjournForm: play.api.libs.json.Reads[AdjournForm] = {
      (__ \ "adjourned_at").readNullable[_root_.org.joda.time.DateTime].map { x => new AdjournForm(adjournedAt = x) }
    }

    implicit def jsonWritesQualityAdjournForm: play.api.libs.json.Writes[AdjournForm] = new play.api.libs.json.Writes[AdjournForm] {
      def writes(x: AdjournForm) = play.api.libs.json.Json.obj(
        "adjourned_at" -> play.api.libs.json.Json.toJson(x.adjournedAt)
      )
    }

    implicit def jsonReadsQualityAgendaItem: play.api.libs.json.Reads[AgendaItem] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "meeting").read[com.gilt.quality.models.Meeting] and
        (__ \ "incident").read[com.gilt.quality.models.Incident] and
        (__ \ "task").read[com.gilt.quality.models.Task]
      )(AgendaItem.apply _)
    }

    implicit def jsonWritesQualityAgendaItem: play.api.libs.json.Writes[AgendaItem] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "meeting").write[com.gilt.quality.models.Meeting] and
        (__ \ "incident").write[com.gilt.quality.models.Incident] and
        (__ \ "task").write[com.gilt.quality.models.Task]
      )(unlift(AgendaItem.unapply _))
    }

    implicit def jsonReadsQualityAgendaItemForm: play.api.libs.json.Reads[AgendaItemForm] = {
      (
        (__ \ "meeting_id").read[Long] and
        (__ \ "incident_id").read[Long] and
        (__ \ "task").read[com.gilt.quality.models.Task]
      )(AgendaItemForm.apply _)
    }

    implicit def jsonWritesQualityAgendaItemForm: play.api.libs.json.Writes[AgendaItemForm] = {
      (
        (__ \ "meeting_id").write[Long] and
        (__ \ "incident_id").write[Long] and
        (__ \ "task").write[com.gilt.quality.models.Task]
      )(unlift(AgendaItemForm.unapply _))
    }

    implicit def jsonReadsQualityAuthenticationForm: play.api.libs.json.Reads[AuthenticationForm] = {
      (__ \ "email").read[String].map { x => new AuthenticationForm(email = x) }
    }

    implicit def jsonWritesQualityAuthenticationForm: play.api.libs.json.Writes[AuthenticationForm] = new play.api.libs.json.Writes[AuthenticationForm] {
      def writes(x: AuthenticationForm) = play.api.libs.json.Json.obj(
        "email" -> play.api.libs.json.Json.toJson(x.email)
      )
    }

    implicit def jsonReadsQualityEmailMessage: play.api.libs.json.Reads[EmailMessage] = {
      (
        (__ \ "subject").read[String] and
        (__ \ "body").read[String]
      )(EmailMessage.apply _)
    }

    implicit def jsonWritesQualityEmailMessage: play.api.libs.json.Writes[EmailMessage] = {
      (
        (__ \ "subject").write[String] and
        (__ \ "body").write[String]
      )(unlift(EmailMessage.unapply _))
    }

    implicit def jsonReadsQualityError: play.api.libs.json.Reads[Error] = {
      (
        (__ \ "code").read[String] and
        (__ \ "message").read[String]
      )(Error.apply _)
    }

    implicit def jsonWritesQualityError: play.api.libs.json.Writes[Error] = {
      (
        (__ \ "code").write[String] and
        (__ \ "message").write[String]
      )(unlift(Error.unapply _))
    }

<<<<<<< HEAD
=======
    implicit def jsonReadsQualityEvent: play.api.libs.json.Reads[Event] = {
      (
        (__ \ "model").read[com.gilt.quality.models.Model] and
        (__ \ "action").read[com.gilt.quality.models.Action] and
        (__ \ "timestamp").read[_root_.org.joda.time.DateTime] and
        (__ \ "url").readNullable[String] and
        (__ \ "data").read[com.gilt.quality.models.EventData]
      )(Event.apply _)
    }

    implicit def jsonWritesQualityEvent: play.api.libs.json.Writes[Event] = {
      (
        (__ \ "model").write[com.gilt.quality.models.Model] and
        (__ \ "action").write[com.gilt.quality.models.Action] and
        (__ \ "timestamp").write[_root_.org.joda.time.DateTime] and
        (__ \ "url").write[scala.Option[String]] and
        (__ \ "data").write[com.gilt.quality.models.EventData]
      )(unlift(Event.unapply _))
    }

    implicit def jsonReadsQualityEventData: play.api.libs.json.Reads[EventData] = {
      (
        (__ \ "model_id").read[Long] and
        (__ \ "summary").read[String]
      )(EventData.apply _)
    }

    implicit def jsonWritesQualityEventData: play.api.libs.json.Writes[EventData] = {
      (
        (__ \ "model_id").write[Long] and
        (__ \ "summary").write[String]
      )(unlift(EventData.unapply _))
    }

    implicit def jsonReadsQualityExternalService: play.api.libs.json.Reads[ExternalService] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "organization").read[com.gilt.quality.models.Organization] and
        (__ \ "name").read[com.gilt.quality.models.ExternalServiceName] and
        (__ \ "url").read[String] and
        (__ \ "username").read[String]
      )(ExternalService.apply _)
    }

    implicit def jsonWritesQualityExternalService: play.api.libs.json.Writes[ExternalService] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "organization").write[com.gilt.quality.models.Organization] and
        (__ \ "name").write[com.gilt.quality.models.ExternalServiceName] and
        (__ \ "url").write[String] and
        (__ \ "username").write[String]
      )(unlift(ExternalService.unapply _))
    }

    implicit def jsonReadsQualityExternalServiceForm: play.api.libs.json.Reads[ExternalServiceForm] = {
      (
        (__ \ "name").read[com.gilt.quality.models.ExternalServiceName] and
        (__ \ "url").read[String] and
        (__ \ "username").read[String] and
        (__ \ "password").read[String]
      )(ExternalServiceForm.apply _)
    }

    implicit def jsonWritesQualityExternalServiceForm: play.api.libs.json.Writes[ExternalServiceForm] = {
      (
        (__ \ "name").write[com.gilt.quality.models.ExternalServiceName] and
        (__ \ "url").write[String] and
        (__ \ "username").write[String] and
        (__ \ "password").write[String]
      )(unlift(ExternalServiceForm.unapply _))
    }

>>>>>>> Add external service to capture information needed for JIRA integration
    implicit def jsonReadsQualityHealthcheck: play.api.libs.json.Reads[Healthcheck] = {
      (__ \ "status").read[String].map { x => new Healthcheck(status = x) }
    }

    implicit def jsonWritesQualityHealthcheck: play.api.libs.json.Writes[Healthcheck] = new play.api.libs.json.Writes[Healthcheck] {
      def writes(x: Healthcheck) = play.api.libs.json.Json.obj(
        "status" -> play.api.libs.json.Json.toJson(x.status)
      )
    }

    implicit def jsonReadsQualityIcons: play.api.libs.json.Reads[Icons] = {
      (
        (__ \ "smiley_url").read[String] and
        (__ \ "frowny_url").read[String]
      )(Icons.apply _)
    }

    implicit def jsonWritesQualityIcons: play.api.libs.json.Writes[Icons] = {
      (
        (__ \ "smiley_url").write[String] and
        (__ \ "frowny_url").write[String]
      )(unlift(Icons.unapply _))
    }

    implicit def jsonReadsQualityIncident: play.api.libs.json.Reads[Incident] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "organization").read[com.gilt.quality.models.Organization] and
        (__ \ "summary").read[String] and
        (__ \ "description").readNullable[String] and
        (__ \ "team").readNullable[com.gilt.quality.models.Team] and
        (__ \ "severity").read[com.gilt.quality.models.Severity] and
        (__ \ "tags").readNullable[scala.collection.Seq[String]].map(_.getOrElse(Nil)) and
        (__ \ "plan").readNullable[com.gilt.quality.models.Plan] and
        (__ \ "created_at").read[_root_.org.joda.time.DateTime]
      )(Incident.apply _)
    }

    implicit def jsonWritesQualityIncident: play.api.libs.json.Writes[Incident] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "organization").write[com.gilt.quality.models.Organization] and
        (__ \ "summary").write[String] and
        (__ \ "description").write[scala.Option[String]] and
        (__ \ "team").write[scala.Option[com.gilt.quality.models.Team]] and
        (__ \ "severity").write[com.gilt.quality.models.Severity] and
        (__ \ "tags").write[scala.collection.Seq[String]] and
        (__ \ "plan").write[scala.Option[com.gilt.quality.models.Plan]] and
        (__ \ "created_at").write[_root_.org.joda.time.DateTime]
      )(unlift(Incident.unapply _))
    }

    implicit def jsonReadsQualityIncidentForm: play.api.libs.json.Reads[IncidentForm] = {
      (
        (__ \ "team_key").readNullable[String] and
        (__ \ "severity").read[com.gilt.quality.models.Severity] and
        (__ \ "summary").read[String] and
        (__ \ "description").readNullable[String] and
        (__ \ "tags").readNullable[scala.collection.Seq[String]].map(_.getOrElse(Nil))
      )(IncidentForm.apply _)
    }

    implicit def jsonWritesQualityIncidentForm: play.api.libs.json.Writes[IncidentForm] = {
      (
        (__ \ "team_key").write[scala.Option[String]] and
        (__ \ "severity").write[com.gilt.quality.models.Severity] and
        (__ \ "summary").write[String] and
        (__ \ "description").write[scala.Option[String]] and
        (__ \ "tags").write[scala.collection.Seq[String]]
      )(unlift(IncidentForm.unapply _))
    }

    implicit def jsonReadsQualityIncidentSummary: play.api.libs.json.Reads[IncidentSummary] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "severity").read[com.gilt.quality.models.Severity] and
        (__ \ "summary").read[String]
      )(IncidentSummary.apply _)
    }

    implicit def jsonWritesQualityIncidentSummary: play.api.libs.json.Writes[IncidentSummary] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "severity").write[com.gilt.quality.models.Severity] and
        (__ \ "summary").write[String]
      )(unlift(IncidentSummary.unapply _))
    }

    implicit def jsonReadsQualityMeeting: play.api.libs.json.Reads[Meeting] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "organization").read[com.gilt.quality.models.Organization] and
        (__ \ "scheduled_at").read[_root_.org.joda.time.DateTime] and
        (__ \ "adjourned_at").readNullable[_root_.org.joda.time.DateTime]
      )(Meeting.apply _)
    }

    implicit def jsonWritesQualityMeeting: play.api.libs.json.Writes[Meeting] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "organization").write[com.gilt.quality.models.Organization] and
        (__ \ "scheduled_at").write[_root_.org.joda.time.DateTime] and
        (__ \ "adjourned_at").write[scala.Option[_root_.org.joda.time.DateTime]]
      )(unlift(Meeting.unapply _))
    }

    implicit def jsonReadsQualityMeetingForm: play.api.libs.json.Reads[MeetingForm] = {
      (__ \ "scheduled_at").read[_root_.org.joda.time.DateTime].map { x => new MeetingForm(scheduledAt = x) }
    }

    implicit def jsonWritesQualityMeetingForm: play.api.libs.json.Writes[MeetingForm] = new play.api.libs.json.Writes[MeetingForm] {
      def writes(x: MeetingForm) = play.api.libs.json.Json.obj(
        "scheduled_at" -> play.api.libs.json.Json.toJson(x.scheduledAt)
      )
    }

    implicit def jsonReadsQualityMeetingPager: play.api.libs.json.Reads[MeetingPager] = {
      (
        (__ \ "meeting").read[com.gilt.quality.models.Meeting] and
        (__ \ "prior_incident").readNullable[com.gilt.quality.models.Incident] and
        (__ \ "next_incident").readNullable[com.gilt.quality.models.Incident]
      )(MeetingPager.apply _)
    }

    implicit def jsonWritesQualityMeetingPager: play.api.libs.json.Writes[MeetingPager] = {
      (
        (__ \ "meeting").write[com.gilt.quality.models.Meeting] and
        (__ \ "prior_incident").write[scala.Option[com.gilt.quality.models.Incident]] and
        (__ \ "next_incident").write[scala.Option[com.gilt.quality.models.Incident]]
      )(unlift(MeetingPager.unapply _))
    }

    implicit def jsonReadsQualityOrganization: play.api.libs.json.Reads[Organization] = {
      (
        (__ \ "key").read[String] and
        (__ \ "name").read[String]
      )(Organization.apply _)
    }

    implicit def jsonWritesQualityOrganization: play.api.libs.json.Writes[Organization] = {
      (
        (__ \ "key").write[String] and
        (__ \ "name").write[String]
      )(unlift(Organization.unapply _))
    }

    implicit def jsonReadsQualityOrganizationForm: play.api.libs.json.Reads[OrganizationForm] = {
      (
        (__ \ "name").read[String] and
        (__ \ "key").readNullable[String]
      )(OrganizationForm.apply _)
    }

    implicit def jsonWritesQualityOrganizationForm: play.api.libs.json.Writes[OrganizationForm] = {
      (
        (__ \ "name").write[String] and
        (__ \ "key").write[scala.Option[String]]
      )(unlift(OrganizationForm.unapply _))
    }

    implicit def jsonReadsQualityPlan: play.api.libs.json.Reads[Plan] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "incident_id").read[Long] and
        (__ \ "body").read[String] and
        (__ \ "grade").readNullable[Int] and
        (__ \ "created_at").read[_root_.org.joda.time.DateTime]
      )(Plan.apply _)
    }

    implicit def jsonWritesQualityPlan: play.api.libs.json.Writes[Plan] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "incident_id").write[Long] and
        (__ \ "body").write[String] and
        (__ \ "grade").write[scala.Option[Int]] and
        (__ \ "created_at").write[_root_.org.joda.time.DateTime]
      )(unlift(Plan.unapply _))
    }

    implicit def jsonReadsQualityPlanForm: play.api.libs.json.Reads[PlanForm] = {
      (
        (__ \ "incident_id").read[Long] and
        (__ \ "body").read[String]
      )(PlanForm.apply _)
    }

    implicit def jsonWritesQualityPlanForm: play.api.libs.json.Writes[PlanForm] = {
      (
        (__ \ "incident_id").write[Long] and
        (__ \ "body").write[String]
      )(unlift(PlanForm.unapply _))
    }

    implicit def jsonReadsQualityStatistic: play.api.libs.json.Reads[Statistic] = {
      (
        (__ \ "team").read[com.gilt.quality.models.Team] and
        (__ \ "total_grades").read[Long] and
        (__ \ "average_grade").readNullable[Int] and
        (__ \ "total_open_incidents").read[Long] and
        (__ \ "total_incidents").read[Long] and
        (__ \ "total_plans").read[Long] and
        (__ \ "plans").readNullable[scala.collection.Seq[com.gilt.quality.models.Plan]].map(_.getOrElse(Nil))
      )(Statistic.apply _)
    }

    implicit def jsonWritesQualityStatistic: play.api.libs.json.Writes[Statistic] = {
      (
        (__ \ "team").write[com.gilt.quality.models.Team] and
        (__ \ "total_grades").write[Long] and
        (__ \ "average_grade").write[scala.Option[Int]] and
        (__ \ "total_open_incidents").write[Long] and
        (__ \ "total_incidents").write[Long] and
        (__ \ "total_plans").write[Long] and
        (__ \ "plans").write[scala.collection.Seq[com.gilt.quality.models.Plan]]
      )(unlift(Statistic.unapply _))
    }

    implicit def jsonReadsQualitySubscription: play.api.libs.json.Reads[Subscription] = {
      (
        (__ \ "id").read[Long] and
        (__ \ "organization").read[com.gilt.quality.models.Organization] and
        (__ \ "user").read[com.gilt.quality.models.User] and
        (__ \ "publication").read[com.gilt.quality.models.Publication]
      )(Subscription.apply _)
    }

    implicit def jsonWritesQualitySubscription: play.api.libs.json.Writes[Subscription] = {
      (
        (__ \ "id").write[Long] and
        (__ \ "organization").write[com.gilt.quality.models.Organization] and
        (__ \ "user").write[com.gilt.quality.models.User] and
        (__ \ "publication").write[com.gilt.quality.models.Publication]
      )(unlift(Subscription.unapply _))
    }

    implicit def jsonReadsQualitySubscriptionForm: play.api.libs.json.Reads[SubscriptionForm] = {
      (
        (__ \ "organization_key").read[String] and
        (__ \ "user_guid").read[java.util.UUID] and
        (__ \ "publication").read[com.gilt.quality.models.Publication]
      )(SubscriptionForm.apply _)
    }

    implicit def jsonWritesQualitySubscriptionForm: play.api.libs.json.Writes[SubscriptionForm] = {
      (
        (__ \ "organization_key").write[String] and
        (__ \ "user_guid").write[java.util.UUID] and
        (__ \ "publication").write[com.gilt.quality.models.Publication]
      )(unlift(SubscriptionForm.unapply _))
    }

    implicit def jsonReadsQualityTeam: play.api.libs.json.Reads[Team] = {
      (
        (__ \ "organization").read[com.gilt.quality.models.Organization] and
        (__ \ "key").read[String] and
        (__ \ "email").readNullable[String] and
        (__ \ "icons").read[com.gilt.quality.models.Icons]
      )(Team.apply _)
    }

    implicit def jsonWritesQualityTeam: play.api.libs.json.Writes[Team] = {
      (
        (__ \ "organization").write[com.gilt.quality.models.Organization] and
        (__ \ "key").write[String] and
        (__ \ "email").write[scala.Option[String]] and
        (__ \ "icons").write[com.gilt.quality.models.Icons]
      )(unlift(Team.unapply _))
    }

    implicit def jsonReadsQualityTeamForm: play.api.libs.json.Reads[TeamForm] = {
      (
        (__ \ "key").read[String] and
        (__ \ "email").readNullable[String] and
        (__ \ "smiley_url").readNullable[String] and
        (__ \ "frowny_url").readNullable[String]
      )(TeamForm.apply _)
    }

    implicit def jsonWritesQualityTeamForm: play.api.libs.json.Writes[TeamForm] = {
      (
        (__ \ "key").write[String] and
        (__ \ "email").write[scala.Option[String]] and
        (__ \ "smiley_url").write[scala.Option[String]] and
        (__ \ "frowny_url").write[scala.Option[String]]
      )(unlift(TeamForm.unapply _))
    }

    implicit def jsonReadsQualityTeamMember: play.api.libs.json.Reads[TeamMember] = {
      (
        (__ \ "team").read[com.gilt.quality.models.Team] and
        (__ \ "user").read[com.gilt.quality.models.User]
      )(TeamMember.apply _)
    }

    implicit def jsonWritesQualityTeamMember: play.api.libs.json.Writes[TeamMember] = {
      (
        (__ \ "team").write[com.gilt.quality.models.Team] and
        (__ \ "user").write[com.gilt.quality.models.User]
      )(unlift(TeamMember.unapply _))
    }

    implicit def jsonReadsQualityTeamMemberSummary: play.api.libs.json.Reads[TeamMemberSummary] = {
      (
        (__ \ "team").read[com.gilt.quality.models.Team] and
        (__ \ "number_members").read[Long]
      )(TeamMemberSummary.apply _)
    }

    implicit def jsonWritesQualityTeamMemberSummary: play.api.libs.json.Writes[TeamMemberSummary] = {
      (
        (__ \ "team").write[com.gilt.quality.models.Team] and
        (__ \ "number_members").write[Long]
      )(unlift(TeamMemberSummary.unapply _))
    }

    implicit def jsonReadsQualityUpdateTeamForm: play.api.libs.json.Reads[UpdateTeamForm] = {
      (
        (__ \ "email").readNullable[String] and
        (__ \ "smiley_url").readNullable[String] and
        (__ \ "frowny_url").readNullable[String]
      )(UpdateTeamForm.apply _)
    }

    implicit def jsonWritesQualityUpdateTeamForm: play.api.libs.json.Writes[UpdateTeamForm] = {
      (
        (__ \ "email").write[scala.Option[String]] and
        (__ \ "smiley_url").write[scala.Option[String]] and
        (__ \ "frowny_url").write[scala.Option[String]]
      )(unlift(UpdateTeamForm.unapply _))
    }

    implicit def jsonReadsQualityUser: play.api.libs.json.Reads[User] = {
      (
        (__ \ "guid").read[java.util.UUID] and
        (__ \ "email").read[String]
      )(User.apply _)
    }

    implicit def jsonWritesQualityUser: play.api.libs.json.Writes[User] = {
      (
        (__ \ "guid").write[java.util.UUID] and
        (__ \ "email").write[String]
      )(unlift(User.unapply _))
    }

    implicit def jsonReadsQualityUserForm: play.api.libs.json.Reads[UserForm] = {
      (__ \ "email").read[String].map { x => new UserForm(email = x) }
    }

    implicit def jsonWritesQualityUserForm: play.api.libs.json.Writes[UserForm] = new play.api.libs.json.Writes[UserForm] {
      def writes(x: UserForm) = play.api.libs.json.Json.obj(
        "email" -> play.api.libs.json.Json.toJson(x.email)
      )
    }
  }
}

package com.gilt.quality {

  class Client(apiUrl: String, apiToken: scala.Option[String] = None) {
    import com.gilt.quality.models.json._

    private val UserAgent = "apidoc:0.6.10 http://www.apidoc.me/gilt/code/quality/0.0.10-dev/play_2_3_client"
    private val logger = play.api.Logger("com.gilt.quality.client")

    logger.info(s"Initializing com.gilt.quality.client for url $apiUrl")

    def agendaItems: AgendaItems = AgendaItems

    def emailMessages: EmailMessages = EmailMessages

<<<<<<< HEAD
=======
    def events: Events = Events

    def externalServices: ExternalServices = ExternalServices

>>>>>>> Add external service to capture information needed for JIRA integration
    def healthchecks: Healthchecks = Healthchecks

    def incidents: Incidents = Incidents

    def meetings: Meetings = Meetings

    def organizations: Organizations = Organizations

    def plans: Plans = Plans

    def statistics: Statistics = Statistics

    def subscriptions: Subscriptions = Subscriptions

    def teams: Teams = Teams

    def users: Users = Users

    object AgendaItems extends AgendaItems {
      override def getAgendaItemsByOrg(
        org: String,
        id: scala.Option[Long] = None,
        meetingId: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        userGuid: scala.Option[java.util.UUID] = None,
        isAdjourned: scala.Option[Boolean] = None,
        task: scala.Option[com.gilt.quality.models.Task] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.AgendaItem]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          meetingId.map("meeting_id" -> _.toString),
          incidentId.map("incident_id" -> _.toString),
          teamKey.map("team_key" -> _),
          userGuid.map("user_guid" -> _.toString),
          isAdjourned.map("is_adjourned" -> _.toString),
          task.map("task" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/agenda_items", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.AgendaItem]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getAgendaItemsByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.AgendaItem]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/agenda_items/${id}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.AgendaItem])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def postAgendaItemsByOrg(agendaItemForm: com.gilt.quality.models.AgendaItemForm, 
        org: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.AgendaItem] = {
        val payload = play.api.libs.json.Json.toJson(agendaItemForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/agenda_items", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.AgendaItem]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteAgendaItemsByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/agenda_items/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object EmailMessages extends EmailMessages {
      override def getEmailMessagesAndMeetingAdjournedByOrgAndMeetingId(
        org: String,
        meetingId: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.EmailMessage]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/email_messages/meeting_adjourned/${meetingId}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.EmailMessage])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

<<<<<<< HEAD
=======
    object Events extends Events {
      override def getByOrg(
        org: String,
        model: scala.Option[com.gilt.quality.models.Model] = None,
        action: scala.Option[com.gilt.quality.models.Action] = None,
        numberHours: scala.Option[Int] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Event]] = {
        val queryParameters = Seq(
          model.map("model" -> _.toString),
          action.map("action" -> _.toString),
          numberHours.map("number_hours" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/events", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Event]]
          case r => throw new FailedRequest(r)
        }
      }
    }

    object ExternalServices extends ExternalServices {
      override def getExternalServicesByOrg(
        org: String,
        id: scala.Option[Long] = None,
        name: scala.Option[com.gilt.quality.models.ExternalServiceName] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.ExternalService]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          name.map("name" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/external_services", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.ExternalService]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getExternalServicesByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.ExternalService]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/external_services/${id}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.ExternalService])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def postExternalServicesByOrg(externalServiceForm: com.gilt.quality.models.ExternalServiceForm, 
        org: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.ExternalService] = {
        val payload = play.api.libs.json.Json.toJson(externalServiceForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/external_services", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.ExternalService]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteExternalServicesByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/external_services/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

>>>>>>> Add external service to capture information needed for JIRA integration
    object Healthchecks extends Healthchecks {
      override def get()(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Healthcheck]] = {
        _executeRequest("GET", s"/_internal_/healthcheck").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Healthcheck])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Incidents extends Incidents {
      override def getByOrg(
        org: String,
        id: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        hasTeam: scala.Option[Boolean] = None,
        hasPlan: scala.Option[Boolean] = None,
        hasGrade: scala.Option[Boolean] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Incident]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          teamKey.map("team_key" -> _),
          hasTeam.map("has_team" -> _.toString),
          hasPlan.map("has_plan" -> _.toString),
          hasGrade.map("has_grade" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/incidents", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Incident]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Incident]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/incidents/${id}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Incident])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def postByOrg(incidentForm: com.gilt.quality.models.IncidentForm, 
        org: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Incident] = {
        val payload = play.api.libs.json.Json.toJson(incidentForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/incidents", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.Incident]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def putByOrgAndId(incidentForm: com.gilt.quality.models.IncidentForm, 
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Incident] = {
        val payload = play.api.libs.json.Json.toJson(incidentForm)

        _executeRequest("PUT", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/incidents/${id}", body = Some(payload)).map {
          case r if r.status == 200 => r.json.as[com.gilt.quality.models.Incident]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/incidents/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Meetings extends Meetings {
      override def getByOrg(
        org: String,
        id: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        agendaItemId: scala.Option[Long] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Meeting]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          incidentId.map("incident_id" -> _.toString),
          agendaItemId.map("agenda_item_id" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/meetings", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Meeting]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Meeting]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/meetings/${id}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Meeting])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def postByOrg(meetingForm: com.gilt.quality.models.MeetingForm, 
        org: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Meeting] = {
        val payload = play.api.libs.json.Json.toJson(meetingForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/meetings", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.Meeting]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def postAdjournByOrgAndId(adjournForm: com.gilt.quality.models.AdjournForm, 
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Meeting] = {
        val payload = play.api.libs.json.Json.toJson(adjournForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/meetings/${id}/adjourn", body = Some(payload)).map {
          case r if r.status == 200 => r.json.as[com.gilt.quality.models.Meeting]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/meetings/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def getPagerByOrgAndIdAndIncidentId(
        org: String,
        id: Long,
        incidentId: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.MeetingPager]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/meetings/${id}/pager/${incidentId}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.MeetingPager])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Organizations extends Organizations {
      override def get(
        id: scala.Option[Long] = None,
        key: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Organization]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          key.map("key" -> _),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/organizations", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Organization]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Organization]] = {
        _executeRequest("GET", s"/organizations/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Organization])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def post(organizationForm: com.gilt.quality.models.OrganizationForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Organization] = {
        val payload = play.api.libs.json.Json.toJson(organizationForm)

        _executeRequest("POST", s"/organizations", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.Organization]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteByKey(
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/organizations/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Plans extends Plans {
      override def getByOrg(
        org: String,
        id: scala.Option[Long] = None,
        incidentId: scala.Option[Long] = None,
        teamKey: scala.Option[String] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Plan]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          incidentId.map("incident_id" -> _.toString),
          teamKey.map("team_key" -> _),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/plans", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Plan]]
          case r => throw new FailedRequest(r)
        }
      }

      override def postByOrg(planForm: com.gilt.quality.models.PlanForm, 
        org: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Plan] = {
        val payload = play.api.libs.json.Json.toJson(planForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/plans", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.Plan]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def putByOrgAndId(planForm: com.gilt.quality.models.PlanForm, 
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Plan] = {
        val payload = play.api.libs.json.Json.toJson(planForm)

        _executeRequest("PUT", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/plans/${id}", body = Some(payload)).map {
          case r if r.status == 200 => r.json.as[com.gilt.quality.models.Plan]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def putGradeByOrgAndId(
        org: String,
        id: Long,
        grade: Int
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Plan] = {
        val payload = play.api.libs.json.Json.obj(
          "grade" -> play.api.libs.json.Json.toJson(grade)
        )

        _executeRequest("PUT", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/plans/${id}/grade", body = Some(payload)).map {
          case r if r.status == 200 => r.json.as[com.gilt.quality.models.Plan]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def getByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Plan]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/plans/${id}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Plan])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteByOrgAndId(
        org: String,
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/plans/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Statistics extends Statistics {
      override def getByOrg(
        org: String,
        teamKey: scala.Option[String] = None,
        numberHours: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Statistic]] = {
        val queryParameters = Seq(
          teamKey.map("team_key" -> _),
          numberHours.map("number_hours" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/statistics", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Statistic]]
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Subscriptions extends Subscriptions {
      override def get(
        id: scala.Option[Long] = None,
        organizationKey: scala.Option[String] = None,
        userGuid: scala.Option[java.util.UUID] = None,
        publication: scala.Option[com.gilt.quality.models.Publication] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Subscription]] = {
        val queryParameters = Seq(
          id.map("id" -> _.toString),
          organizationKey.map("organization_key" -> _),
          userGuid.map("user_guid" -> _.toString),
          publication.map("publication" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/subscriptions", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Subscription]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Subscription]] = {
        _executeRequest("GET", s"/subscriptions/${id}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Subscription])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def post(subscriptionForm: com.gilt.quality.models.SubscriptionForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Subscription] = {
        val payload = play.api.libs.json.Json.toJson(subscriptionForm)

        _executeRequest("POST", s"/subscriptions", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.Subscription]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteById(
        id: Long
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/subscriptions/${id}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Teams extends Teams {
      override def getByOrg(
        org: String,
        key: scala.Option[String] = None,
        userGuid: scala.Option[java.util.UUID] = None,
        excludeUserGuid: scala.Option[java.util.UUID] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Team]] = {
        val queryParameters = Seq(
          key.map("key" -> _),
          userGuid.map("user_guid" -> _.toString),
          excludeUserGuid.map("exclude_user_guid" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.Team]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getByOrgAndKey(
        org: String,
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Team]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.Team])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def postByOrg(teamForm: com.gilt.quality.models.TeamForm, 
        org: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Team] = {
        val payload = play.api.libs.json.Json.toJson(teamForm)

        _executeRequest("POST", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.Team]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def putByOrgAndKey(updateTeamForm: com.gilt.quality.models.UpdateTeamForm, 
        org: String,
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Team] = {
        val payload = play.api.libs.json.Json.toJson(updateTeamForm)

        _executeRequest("PUT", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}", body = Some(payload)).map {
          case r if r.status == 200 => r.json.as[com.gilt.quality.models.Team]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteByOrgAndKey(
        org: String,
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def getMemberSummaryByOrgAndKey(
        org: String,
        key: String
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.TeamMemberSummary]] = {
        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}/member_summary").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.TeamMemberSummary])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def getMembersByOrgAndKey(
        org: String,
        key: String,
        userGuid: scala.Option[java.util.UUID] = None,
        limit: scala.Option[Int] = None,
        offset: scala.Option[Int] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.TeamMember]] = {
        val queryParameters = Seq(
          userGuid.map("user_guid" -> _.toString),
          limit.map("limit" -> _.toString),
          offset.map("offset" -> _.toString)
        ).flatten

        _executeRequest("GET", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}/members", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.TeamMember]]
          case r => throw new FailedRequest(r)
        }
      }

      override def putMembersByOrgAndKeyAndUserGuid(
        org: String,
        key: String,
        userGuid: java.util.UUID
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.TeamMember] = {
        _executeRequest("PUT", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}/members/${userGuid}").map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.TeamMember]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def deleteMembersByOrgAndKeyAndUserGuid(
        org: String,
        key: String,
        userGuid: java.util.UUID
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]] = {
        _executeRequest("DELETE", s"/${play.utils.UriEncoding.encodePathSegment(org, "UTF-8")}/teams/${play.utils.UriEncoding.encodePathSegment(key, "UTF-8")}/members/${userGuid}").map {
          case r if r.status == 204 => Some(Unit)
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }
    }

    object Users extends Users {
      override def get(
        guid: scala.Option[java.util.UUID] = None,
        email: scala.Option[String] = None
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.User]] = {
        val queryParameters = Seq(
          guid.map("guid" -> _.toString),
          email.map("email" -> _)
        ).flatten

        _executeRequest("GET", s"/users", queryParameters = queryParameters).map {
          case r if r.status == 200 => r.json.as[scala.collection.Seq[com.gilt.quality.models.User]]
          case r => throw new FailedRequest(r)
        }
      }

      override def getByGuid(
        guid: java.util.UUID
      )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.User]] = {
        _executeRequest("GET", s"/users/${guid}").map {
          case r if r.status == 200 => Some(r.json.as[com.gilt.quality.models.User])
          case r if r.status == 404 => None
          case r => throw new FailedRequest(r)
        }
      }

      override def postAuthenticate(authenticationForm: com.gilt.quality.models.AuthenticationForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.User] = {
        val payload = play.api.libs.json.Json.toJson(authenticationForm)

        _executeRequest("POST", s"/users/authenticate", body = Some(payload)).map {
          case r if r.status == 200 => r.json.as[com.gilt.quality.models.User]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }

      override def post(userForm: com.gilt.quality.models.UserForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.User] = {
        val payload = play.api.libs.json.Json.toJson(userForm)

        _executeRequest("POST", s"/users", body = Some(payload)).map {
          case r if r.status == 201 => r.json.as[com.gilt.quality.models.User]
          case r if r.status == 409 => throw new com.gilt.quality.error.ErrorsResponse(r)
          case r => throw new FailedRequest(r)
        }
      }
    }

    def _requestHolder(path: String): play.api.libs.ws.WSRequestHolder = {
      import play.api.Play.current

      val holder = play.api.libs.ws.WS.url(apiUrl + path).withHeaders("User-Agent" -> UserAgent)
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

    def _executeRequest(
      method: String,
      path: String,
      queryParameters: Seq[(String, String)] = Seq.empty,
      body: Option[play.api.libs.json.JsValue] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.libs.ws.WSResponse] = {
      method.toUpperCase match {
        case "GET" => {
          _logRequest("GET", _requestHolder(path).withQueryString(queryParameters:_*)).get()
        }
        case "POST" => {
          _logRequest("POST", _requestHolder(path).withQueryString(queryParameters:_*)).post(body.getOrElse(play.api.libs.json.Json.obj()))
        }
        case "PUT" => {
          _logRequest("PUT", _requestHolder(path).withQueryString(queryParameters:_*)).put(body.getOrElse(play.api.libs.json.Json.obj()))
        }
        case "PATCH" => {
          _logRequest("PATCH", _requestHolder(path).withQueryString(queryParameters:_*)).patch(body.getOrElse(play.api.libs.json.Json.obj()))
        }
        case "DELETE" => {
          _logRequest("DELETE", _requestHolder(path).withQueryString(queryParameters:_*)).delete()
        }
         case "HEAD" => {
          _logRequest("HEAD", _requestHolder(path).withQueryString(queryParameters:_*)).head()
        }
        case _ => {
          _logRequest(method, _requestHolder(path).withQueryString(queryParameters:_*))
          sys.error("Unsupported method[%s]".format(method))
        }
      }
    }

  }

  trait AgendaItems {
    /**
     * Search agenda items for a given meeting. Results are always paginated.
     */
    def getAgendaItemsByOrg(
      org: String,
      id: scala.Option[Long] = None,
      meetingId: scala.Option[Long] = None,
      incidentId: scala.Option[Long] = None,
      teamKey: scala.Option[String] = None,
      userGuid: scala.Option[java.util.UUID] = None,
      isAdjourned: scala.Option[Boolean] = None,
      task: scala.Option[com.gilt.quality.models.Task] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.AgendaItem]]

    def getAgendaItemsByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.AgendaItem]]

    /**
     * Creates an agenda item for this meeting.
     */
    def postAgendaItemsByOrg(agendaItemForm: com.gilt.quality.models.AgendaItemForm, 
      org: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.AgendaItem]

    def deleteAgendaItemsByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

  trait EmailMessages {
    def getEmailMessagesAndMeetingAdjournedByOrgAndMeetingId(
      org: String,
      meetingId: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.EmailMessage]]
  }

<<<<<<< HEAD
=======
  trait Events {
    /**
     * Search all events. Results are always paginated. Events are sorted in time order
     * - the first record is the most recent event.
     */
    def getByOrg(
      org: String,
      model: scala.Option[com.gilt.quality.models.Model] = None,
      action: scala.Option[com.gilt.quality.models.Action] = None,
      numberHours: scala.Option[Int] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Event]]
  }

  trait ExternalServices {
    def getExternalServicesByOrg(
      org: String,
      id: scala.Option[Long] = None,
      name: scala.Option[com.gilt.quality.models.ExternalServiceName] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.ExternalService]]

    def getExternalServicesByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.ExternalService]]

    def postExternalServicesByOrg(externalServiceForm: com.gilt.quality.models.ExternalServiceForm, 
      org: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.ExternalService]

    def deleteExternalServicesByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

>>>>>>> Add external service to capture information needed for JIRA integration
  trait Healthchecks {
    def get()(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Healthcheck]]
  }

  trait Incidents {
    /**
     * Search all incidents. Results are always paginated.
     */
    def getByOrg(
      org: String,
      id: scala.Option[Long] = None,
      teamKey: scala.Option[String] = None,
      hasTeam: scala.Option[Boolean] = None,
      hasPlan: scala.Option[Boolean] = None,
      hasGrade: scala.Option[Boolean] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Incident]]

    /**
     * Returns information about the incident with this specific id.
     */
    def getByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Incident]]

    /**
     * Create a new incident.
     */
    def postByOrg(incidentForm: com.gilt.quality.models.IncidentForm, 
      org: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Incident]

    /**
     * Updates an incident.
     */
    def putByOrgAndId(incidentForm: com.gilt.quality.models.IncidentForm, 
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Incident]

    def deleteByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

  trait Meetings {
    /**
     * Search all meetings. Results are always paginated.
     */
    def getByOrg(
      org: String,
      id: scala.Option[Long] = None,
      incidentId: scala.Option[Long] = None,
      agendaItemId: scala.Option[Long] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Meeting]]

    def getByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Meeting]]

    /**
     * Creates a meeting. In general meetings are created automatically
     */
    def postByOrg(meetingForm: com.gilt.quality.models.MeetingForm, 
      org: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Meeting]

    /**
     * Records that a meeting has been adjourned. Will return a validation error if the
     * meeting had previously been adjourned
     */
    def postAdjournByOrgAndId(adjournForm: com.gilt.quality.models.AdjournForm, 
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Meeting]

    def deleteByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]

    /**
     * Get information on paging through incidents (e.g. the prior or next incident in
     * a given meeting)
     */
    def getPagerByOrgAndIdAndIncidentId(
      org: String,
      id: Long,
      incidentId: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.MeetingPager]]
  }

  trait Organizations {
    /**
     * Search all organizations. Results are always paginated.
     */
    def get(
      id: scala.Option[Long] = None,
      key: scala.Option[String] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Organization]]

    def getByKey(
      key: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Organization]]

    def post(organizationForm: com.gilt.quality.models.OrganizationForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Organization]

    def deleteByKey(
      key: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

  trait Plans {
    /**
     * Search all plans. Results are always paginated.
     */
    def getByOrg(
      org: String,
      id: scala.Option[Long] = None,
      incidentId: scala.Option[Long] = None,
      teamKey: scala.Option[String] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Plan]]

    /**
     * Create a plan.
     */
    def postByOrg(planForm: com.gilt.quality.models.PlanForm, 
      org: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Plan]

    /**
     * Update a plan.
     */
    def putByOrgAndId(planForm: com.gilt.quality.models.PlanForm, 
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Plan]

    /**
     * Update the grade assigned to a plan.
     */
    def putGradeByOrgAndId(
      org: String,
      id: Long,
      grade: Int
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Plan]

    /**
     * Get a single plan.
     */
    def getByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Plan]]

    /**
     * Delete a plan.
     */
    def deleteByOrgAndId(
      org: String,
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

  trait Statistics {
    /**
     * Retrieve team statistics for all or one team.
     */
    def getByOrg(
      org: String,
      teamKey: scala.Option[String] = None,
      numberHours: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Statistic]]
  }

  trait Subscriptions {
    /**
     * Search for a specific subscription.
     */
    def get(
      id: scala.Option[Long] = None,
      organizationKey: scala.Option[String] = None,
      userGuid: scala.Option[java.util.UUID] = None,
      publication: scala.Option[com.gilt.quality.models.Publication] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Subscription]]

    /**
     * Returns information about this subscription.
     */
    def getById(
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Subscription]]

    /**
     * Create a new subscription.
     */
    def post(subscriptionForm: com.gilt.quality.models.SubscriptionForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Subscription]

    def deleteById(
      id: Long
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

  trait Teams {
    /**
     * Search all teams. Results are always paginated.
     */
    def getByOrg(
      org: String,
      key: scala.Option[String] = None,
      userGuid: scala.Option[java.util.UUID] = None,
      excludeUserGuid: scala.Option[java.util.UUID] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.Team]]

    /**
     * Returns information about the team with this specific key.
     */
    def getByOrgAndKey(
      org: String,
      key: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.Team]]

    /**
     * Create a new team.
     */
    def postByOrg(teamForm: com.gilt.quality.models.TeamForm, 
      org: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Team]

    /**
     * Update a team.
     */
    def putByOrgAndKey(updateTeamForm: com.gilt.quality.models.UpdateTeamForm, 
      org: String,
      key: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.Team]

    def deleteByOrgAndKey(
      org: String,
      key: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]

    /**
     * Summary information about this teams members
     */
    def getMemberSummaryByOrgAndKey(
      org: String,
      key: String
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.TeamMemberSummary]]

    /**
     * Lists the members of this team
     */
    def getMembersByOrgAndKey(
      org: String,
      key: String,
      userGuid: scala.Option[java.util.UUID] = None,
      limit: scala.Option[Int] = None,
      offset: scala.Option[Int] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.TeamMember]]

    /**
     * Adds the specified user to this team
     */
    def putMembersByOrgAndKeyAndUserGuid(
      org: String,
      key: String,
      userGuid: java.util.UUID
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.TeamMember]

    /**
     * Removes this user from this team
     */
    def deleteMembersByOrgAndKeyAndUserGuid(
      org: String,
      key: String,
      userGuid: java.util.UUID
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[Unit]]
  }

  trait Users {
    /**
     * Search for a specific user. You must specify at least 1 parameter - either a
     * guid or email - and will receive back either 0 or 1 users.
     */
    def get(
      guid: scala.Option[java.util.UUID] = None,
      email: scala.Option[String] = None
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.collection.Seq[com.gilt.quality.models.User]]

    /**
     * Returns information about the user with this guid.
     */
    def getByGuid(
      guid: java.util.UUID
    )(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Option[com.gilt.quality.models.User]]

    /**
     * Used to authenticate a user with an email address and password. Successful
     * authentication returns an instance of the user model. Failed authorizations of
     * any kind are returned as a generic error with code user_authorization_failed.
     */
    def postAuthenticate(authenticationForm: com.gilt.quality.models.AuthenticationForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.User]

    /**
     * Create a new user.
     */
    def post(userForm: com.gilt.quality.models.UserForm)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[com.gilt.quality.models.User]
  }

  case class FailedRequest(
    response: play.api.libs.ws.WSResponse,
    message: Option[String] = None
  ) extends Exception(message.getOrElse(response.status + ": " + response.body))

  package error {

    import com.gilt.quality.models.json._

    case class ErrorsResponse(
      response: play.api.libs.ws.WSResponse,
      message: Option[String] = None
    ) extends Exception(message.getOrElse(response.status + ": " + response.body)){
      import com.gilt.quality.models.json._
      lazy val errors = response.json.as[scala.collection.Seq[com.gilt.quality.models.Error]]
    }
  }

  object Bindables {

    import play.api.mvc.{PathBindable, QueryStringBindable}
    import org.joda.time.{DateTime, LocalDate}
    import org.joda.time.format.ISODateTimeFormat
    import com.gilt.quality.models._

    // Type: date-time-iso8601
    implicit val pathBindableTypeDateTimeIso8601 = new PathBindable.Parsing[DateTime](
      ISODateTimeFormat.dateTimeParser.parseDateTime(_), _.toString, (key: String, e: Exception) => s"Error parsing date time $key. Example: 2014-04-29T11:56:52Z"
    )

    // Type: date-iso8601
    implicit val pathBindableTypeDateIso8601 = new PathBindable.Parsing[LocalDate](
      ISODateTimeFormat.yearMonthDay.parseLocalDate(_), _.toString, (key: String, e: Exception) => s"Error parsing date $key. Example: 2014-04-29"
    )

<<<<<<< HEAD
=======
    // Enum: Action
    private val enumActionNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${Action.all.mkString(", ")}"

    implicit val pathBindableEnumAction = new PathBindable.Parsing[Action] (
      Action.fromString(_).get, _.toString, enumActionNotFound
    )

    implicit val queryStringBindableEnumAction = new QueryStringBindable.Parsing[Action](
      Action.fromString(_).get, _.toString, enumActionNotFound
    )

    // Enum: ExternalServiceName
    private val enumExternalServiceNameNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${ExternalServiceName.all.mkString(", ")}"

    implicit val pathBindableEnumExternalServiceName = new PathBindable.Parsing[ExternalServiceName] (
      ExternalServiceName.fromString(_).get, _.toString, enumExternalServiceNameNotFound
    )

    implicit val queryStringBindableEnumExternalServiceName = new QueryStringBindable.Parsing[ExternalServiceName](
      ExternalServiceName.fromString(_).get, _.toString, enumExternalServiceNameNotFound
    )

    // Enum: Model
    private val enumModelNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${Model.all.mkString(", ")}"

    implicit val pathBindableEnumModel = new PathBindable.Parsing[Model] (
      Model.fromString(_).get, _.toString, enumModelNotFound
    )

    implicit val queryStringBindableEnumModel = new QueryStringBindable.Parsing[Model](
      Model.fromString(_).get, _.toString, enumModelNotFound
    )

>>>>>>> Add external service to capture information needed for JIRA integration
    // Enum: Publication
    private val enumPublicationNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${Publication.all.mkString(", ")}"

    implicit val pathBindableEnumPublication = new PathBindable.Parsing[Publication] (
      Publication.fromString(_).get, _.toString, enumPublicationNotFound
    )

    implicit val queryStringBindableEnumPublication = new QueryStringBindable.Parsing[Publication](
      Publication.fromString(_).get, _.toString, enumPublicationNotFound
    )

    // Enum: Severity
    private val enumSeverityNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${Severity.all.mkString(", ")}"

    implicit val pathBindableEnumSeverity = new PathBindable.Parsing[Severity] (
      Severity.fromString(_).get, _.toString, enumSeverityNotFound
    )

    implicit val queryStringBindableEnumSeverity = new QueryStringBindable.Parsing[Severity](
      Severity.fromString(_).get, _.toString, enumSeverityNotFound
    )

    // Enum: Task
    private val enumTaskNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${Task.all.mkString(", ")}"

    implicit val pathBindableEnumTask = new PathBindable.Parsing[Task] (
      Task.fromString(_).get, _.toString, enumTaskNotFound
    )

    implicit val queryStringBindableEnumTask = new QueryStringBindable.Parsing[Task](
      Task.fromString(_).get, _.toString, enumTaskNotFound
    )

  }

}