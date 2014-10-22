package actors

import com.gilt.quality.models.Publication
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import play.api.Logger
import play.api.Play.current

object MainActor {
  def props() = Props(new MainActor("main"))
}

class MainActor(name: String) extends Actor with ActorLogging {
  import scala.concurrent.duration._

  val meetingActor = Akka.system.actorOf(Props[MeetingActor], name = s"$name:meetingActor")
  val emailActor = Akka.system.actorOf(Props[EmailActor], name = s"$name:emailActor")

  Akka.system.scheduler.schedule(15.seconds, 1.minutes, meetingActor, InternalMeetingMessage.SyncOrganizationMeetings)
  Akka.system.scheduler.schedule(20.seconds, 1.minutes, meetingActor, InternalMeetingMessage.SyncMeetings)
  Akka.system.scheduler.schedule(25.seconds, 15.minutes, meetingActor, InternalMeetingMessage.SyncIncidents)

  def receive = akka.event.LoggingReceive {
    case MeetingMessage.IncidentCreated(incidentId) => {
      Logger.info(s"MainActor: Received MeetingMessage.IncidentCreated($incidentId)")
      meetingActor ! InternalMeetingMessage.SyncIncident(incidentId)
      emailActor ! EmailMessage.Incident(Publication.Incidentscreate, incidentId)
    }

    case MeetingMessage.IncidentUpdated(incidentId) => {
      Logger.info(s"MainActor: Received MeetingMessage.IncidentUpdated($incidentId)")
      meetingActor ! InternalMeetingMessage.SyncIncident(incidentId)
      emailActor ! EmailMessage.Incident(Publication.Incidentsupdate, incidentId)
    }

    case MeetingMessage.PlanCreated(planId) => {
      Logger.info(s"MainActor: Received MeetingMessage.PlanCreated($planId)")
      emailActor ! EmailMessage.Plan(Publication.Planscreate, planId)
    }

    case MeetingMessage.PlanUpdated(planId) => {
      Logger.info(s"MainActor: Received MeetingMessage.PlanUpdated($planId)")
      emailActor ! EmailMessage.Plan(Publication.Plansupdate, planId)
    }

    case MeetingMessage.IncidentTeamUpdated(incidentId) => {
      Logger.info(s"MainActor: Received MeetingMessage.IncidentTeamUpdated($incidentId)")
      // TODO: Send Email to the team if this incident is an upcoming meeting
    }

    case MeetingMessage.AgendaItemCreated(agendaItemId) => {
      Logger.info(s"MainActor: Received MeetingMessage.AgendaItemCreated($agendaItemId)")
      meetingActor ! MeetingMessage.AgendaItemCreated(agendaItemId)
    }

    case InternalMeetingMessage.SyncIncident(incidentId) => {
      Logger.info(s"MainActor: Received InternalMeetingMessage.SyncIncident($incidentId)")
      meetingActor ! InternalMeetingMessage.SyncIncident(incidentId)
    }

    case m: Any => {
      Logger.error("Main actor got an unhandled message: " + m)
    }
  }
}
