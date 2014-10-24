package actors

import com.gilt.quality.models.{AdjournForm, PlanForm, Task, User}
import db.{AgendaItemsDao, FullIncidentForm, IncidentsDao, MeetingsDao, UsersDao, Util}
import org.joda.time.DateTime
import java.util.UUID
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class DatabaseSpec extends FunSpec with ShouldMatchers {

  new play.core.StaticApplication(new java.io.File("."))

  it("autoAdjournMeetings") {
    val now = new DateTime()
    val org = Util.createOrganization()
    val meetingTomorrow = MeetingsDao.upsert(org, now.plusHours(24))
    val meetingLastHour = MeetingsDao.upsert(org, now.plusHours(-1))
    val meeting6HoursAgo = MeetingsDao.upsert(org, now.plusHours(-6))

    Database.autoAdjournMeetings()

    MeetingsDao.findAll(Some(org), isAdjourned = Some(true)).map(_.id) should be(Seq(meeting6HoursAgo.id))
  }

  it("eachMeetingIncident") {
    // Actors look for meetings that ended in past 12 hours
    val now = new DateTime()
    val org = Util.createOrganization()
    val meetingLastHour = MeetingsDao.upsert(org, now.plusHours(-1))

    val incident = Util.createIncident(org)
    MeetingsDao.upsertAgendaItem(meetingLastHour, incident, Task.ReviewTeam)

    var incidentIds = scala.collection.mutable.ListBuffer[Long]()
    Database.eachMeetingIncident(meetingLastHour.id, i =>
      incidentIds.append(i.id)
    )
    incidentIds should be(Seq(incident.id))
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
    val meeting3 = MeetingsDao.upsert(org, meeting1.scheduledAt.plusWeeks(2))

    MeetingsDao.upsertAgendaItem(meeting1, incident, Task.ReviewTeam)

    // Task remains review team assignment as long as the meeting has
    // not been adjourned.
    Database.nextTask(incident) should be(None)

    // Assign a team. Task remains none until the meeting is adjourned
    val team = Util.createTeam(org)
    val incidentWithTeam = IncidentsDao.update(UsersDao.Default, incident, FullIncidentForm(org, form.copy(teamKey = Some(team.key))))
    Database.nextTask(incident) should be(None)

    // Adjourn the first meeting; task should move on to review
    MeetingsDao.adjourn(UsersDao.Default, meeting1, AdjournForm())
    Database.nextTask(incidentWithTeam) should be(Some(Task.ReviewPlan))

    MeetingsDao.upsertAgendaItem(meeting2, incidentWithTeam, Task.ReviewPlan)

    // Adjourn the first meeting. Plan wasn't completed so next task
    // should remain ReviewPlan
    MeetingsDao.adjourn(UsersDao.Default, meeting2, AdjournForm())

    Database.nextTask(incidentWithTeam) should be(Some(Task.ReviewPlan))
    MeetingsDao.upsertAgendaItem(meeting3, incidentWithTeam, Task.ReviewPlan)

    // Task remains none until meeting3 is adjourned
    Database.nextTask(incidentWithTeam) should be(None)

    val plan = Util.createPlan(
      org,
      Some(
        PlanForm(
          incidentId = incidentWithTeam.id,
          body = "test"
        )
      )
    )

    val incidentWithPlan = IncidentsDao.findById(incidentWithTeam.id).get
    Database.nextTask(incidentWithPlan) should be(None)

    Util.createGrade(plan, 100)

    val incidentWithGradedPlan = IncidentsDao.findById(incidentWithTeam.id).get
    Database.nextTask(incidentWithGradedPlan) should be(None)

    MeetingsDao.adjourn(UsersDao.Default, meeting3, AdjournForm())
    Database.nextTask(incidentWithGradedPlan) should be(None)
  }

}
