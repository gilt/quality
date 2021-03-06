package actors

import com.gilt.quality.v0.models.Publication
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Logger
import play.api.Play.current

object MainActor {
  def props() = Props(new MainActor("main"))

  case class IncidentCreated(incidentId: Long)
  case class IncidentUpdated(incidentId: Long)
  case class IncidentTeamUpdated(incidentId: Long)
  case class PlanCreated(planId: Long)
  case class PlanUpdated(planId: Long)
  case class MeetingAdjourned(meetingId: Long)
}


class MainActor(name: String) extends Actor with ActorLogging {
  import scala.concurrent.duration._

  val meetingActor = Akka.system.actorOf(Props[MeetingActor], name = s"$name:meetingActor")
  val emailActor = Akka.system.actorOf(Props[EmailActor], name = s"$name:emailActor")

  Akka.system.scheduler.schedule(15.seconds, 1.minutes, meetingActor, MeetingMessage.EnsureUpcomingMeetings)
  Akka.system.scheduler.schedule(20.seconds, 1.minutes, meetingActor, MeetingMessage.AutoAdjournMeetings)
  Akka.system.scheduler.schedule(25.seconds, 15.minutes, meetingActor, MeetingMessage.SyncIncidents)

  def receive = akka.event.LoggingReceive {
    case MainActor.IncidentCreated(incidentId) => {
      Logger.info(s"MainActor: Received MainActor.IncidentCreated($incidentId)")
      meetingActor ! MeetingMessage.SyncIncident(incidentId)
      emailActor ! EmailActorMessage.Incident(Publication.IncidentsCreate, incidentId)
    }

    case MainActor.IncidentUpdated(incidentId) => {
      Logger.info(s"MainActor: Received MainActor.IncidentUpdated($incidentId)")
      meetingActor ! MeetingMessage.SyncIncident(incidentId)
      emailActor ! EmailActorMessage.Incident(Publication.IncidentsUpdate, incidentId)
    }

    case MainActor.PlanCreated(planId) => {
      Logger.info(s"MainActor: Received MainActor.PlanCreated($planId)")
      emailActor ! EmailActorMessage.Plan(Publication.PlansCreate, planId)
    }

    case MainActor.PlanUpdated(planId) => {
      Logger.info(s"MainActor: Received MainActor.PlanUpdated($planId)")
      emailActor ! EmailActorMessage.Plan(Publication.PlansUpdate, planId)
    }

    case MainActor.IncidentTeamUpdated(incidentId) => {
      Logger.info(s"MainActor: Received MainActor.IncidentTeamUpdated($incidentId)")
      emailActor ! EmailActorMessage.IncidentTeamUpdated(Publication.IncidentsTeamUpdate, incidentId)
    }

    case MainActor.MeetingAdjourned(meetingId: Long) => {
      Logger.info(s"MainActor: Received MainActor.MeetingAdjourned($meetingId)")
      meetingActor ! MeetingMessage.SyncMeeting(meetingId)
      emailActor ! EmailActorMessage.MeetingAdjourned(meetingId)
    }

    case MeetingMessage.SyncIncident(incidentId) => {
      Logger.info(s"MainActor: Received MeetingMessage.SyncIncident($incidentId)")
      meetingActor ! MeetingMessage.SyncIncident(incidentId)
    }

    case m: Any => {
      Logger.error("Main actor got an unhandled message: " + m)
    }
  }
}
