package actors

import com.gilt.quality.models.Publication
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
  case class AgendaItemCreated(agendaItemId: Long)
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
      emailActor ! EmailMessage.Incident(Publication.IncidentsCreate, incidentId)
    }

    case MainActor.IncidentUpdated(incidentId) => {
      Logger.info(s"MainActor: Received MainActor.IncidentUpdated($incidentId)")
      meetingActor ! MeetingMessage.SyncIncident(incidentId)
      emailActor ! EmailMessage.Incident(Publication.IncidentsUpdate, incidentId)
    }

    case MainActor.PlanCreated(planId) => {
      Logger.info(s"MainActor: Received MainActor.PlanCreated($planId)")
      emailActor ! EmailMessage.Plan(Publication.PlansCreate, planId)
    }

    case MainActor.PlanUpdated(planId) => {
      Logger.info(s"MainActor: Received MainActor.PlanUpdated($planId)")
      emailActor ! EmailMessage.Plan(Publication.PlansUpdate, planId)
    }

    case MainActor.MeetingAdjourned(meetingId: Long) => {
      Logger.info(s"MainActor: Received MainActor.MeetingAdjourned($meetingId)")
      // TODO: what actions do we want to take when a meeting ends?
    }

    case MainActor.IncidentTeamUpdated(incidentId) => {
      Logger.info(s"MainActor: Received MainActor.IncidentTeamUpdated($incidentId)")
      // TODO: Send Email to the team if this incident is an upcoming meeting
    }

    case MainActor.AgendaItemCreated(agendaItemId) => {
      Logger.info(s"MainActor: Received MainActor.AgendaItemCreated($agendaItemId)")
      meetingActor ! MainActor.AgendaItemCreated(agendaItemId)
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
