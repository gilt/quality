package actors

import com.gilt.quality.models.Task
import db.{FullIncidentForm, IncidentsDao, MeetingsDao, User, Util}
import java.util.UUID
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class DatabaseSpec extends FunSpec with ShouldMatchers {

  it("nextTask is reviewTeam even if an incident is created with a team") {
    val org = Util.createOrganization()
    val team = Util.createTeam()
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
    val team = Util.createTeam()
    val incidentWithTeam = IncidentsDao.update(User.Default, incident, FullIncidentForm(org, form.copy(teamKey = Some(team.key))))
    Database.nextTask(incidentWithTeam) should be(Some(Task.ReviewPlan))

    MeetingsDao.upsertAgendaItem(meeting2, incidentWithTeam, Task.ReviewPlan)
    Database.nextTask(incidentWithTeam) should be(None)
  }

}
