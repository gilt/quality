package actors

import com.gilt.quality.models.Task
import db.{FullIncidentForm, IncidentsDao, MeetingsDao, User, Util}
import org.joda.time.DateTime
import java.util.UUID
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}

class DatabaseSpec extends FunSpec with ShouldMatchers {

  new play.core.StaticApplication(new java.io.File("."))

  it("syncMeetings") {
    //implicit val system = ActorSystem("DatabaseSpecActorSystem")
    //val actorRef = TestActorRef(Props(new MainActor("test")))
    //expectMsg(MeetingMessage.SyncIncident(1))
    val lastWeek = (new DateTime()).plusWeeks(-1)
    val nextWeek = (new DateTime()).plusWeeks(1)
    val org = Util.createOrganization()
    val meetingLastWeek = MeetingsDao.upsert(org, lastWeek)
    val meetingNextWeek = MeetingsDao.upsert(org, nextWeek)

    val incident = Util.createIncident(org)

    MeetingsDao.upsertAgendaItem(meetingLastWeek, incident, Task.ReviewTeam)

    Database.syncMeetings()
    val meetingIds2 = MeetingsDao.findAll(incidentId = Some(incident.id)).map(_.id)
    meetingIds2.contains(meetingLastWeek.id) should be(true)
    meetingIds2.contains(meetingNextWeek.id) should be(true)
  }

  it("ensureAllOrganizationHaveUpcomingMeetings") {
    val orgs = Seq(Util.createOrganization(), Util.createOrganization())
    Database.ensureAllOrganizationHaveUpcomingMeetings()
    orgs.foreach { org =>
      MeetingsDao.findAll(
        isUpcoming = Some(true),
        org = Some(org)
      ).size >= 2 should be(true)
    }
  }

  it("ensureOrganizationHasUpcomingMeetings") {
    val org = Util.createOrganization()
    Database.ensureOrganizationHasUpcomingMeetings(org)
    MeetingsDao.findAll(
      org = Some(org),
      isUpcoming = Some(true)
    ).size >= 2 should be(true)
  }

  it("bestNextMeetingForOrg") {
    val now = new DateTime()
    val org = Util.createOrganization()
    Database.ensureOrganizationHasUpcomingMeetings(org)
    val meeting = Database.bestNextMeetingForOrg(org).getOrElse {
      sys.error("No meetings created")
    }
    meeting.scheduledAt.isAfter(now) should be(true)
    meeting.scheduledAt.isBefore(now.plusDays(8)) should be(true)
  }

  it("nextTask is reviewTeam even if an incident is created with a team") {
    val org = Util.createOrganization()
    val team = Util.createTeam(org)
    val form = Util.createIncidentForm().copy(teamKey = Some(team.key))
    val incident = Util.createIncident(org, form)
    Database.nextTask(incident) should be(Some(Task.ReviewTeam))
  }

  it("nextTask workflow") {
    val org = Util.createOrganization()
    val form = Util.createIncidentForm()

    // First task is to review team assignment
    val incident = Util.createIncident(org, form)
    Database.nextTask(incident) should be(Some(Task.ReviewTeam))
    Database.nextTask(incident) should be(Some(Task.ReviewTeam))

    val meeting1 = Util.createMeeting(org)
    val meeting2 = MeetingsDao.upsert(org, meeting1.scheduledAt.plusWeeks(1))

    MeetingsDao.upsertAgendaItem(meeting1, incident, Task.ReviewTeam)

    // Task remains review team assignment until the incident has a team
    Database.nextTask(incident) should be(Some(Task.ReviewTeam))

    // Once we have a team assigned, next task is review plan
    val team = Util.createTeam(org)
    val incidentWithTeam = IncidentsDao.update(User.Default, incident, FullIncidentForm(org, form.copy(teamKey = Some(team.key))))
    Database.nextTask(incidentWithTeam) should be(Some(Task.ReviewPlan))

    MeetingsDao.upsertAgendaItem(meeting2, incidentWithTeam, Task.ReviewPlan)
    Database.nextTask(incidentWithTeam) should be(None)
  }

}
