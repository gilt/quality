package actors

import com.gilt.quality.models._
import core.DateHelper
import db._
import java.util.UUID
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class MeetingAdjournedEmailSpec extends FunSpec with ShouldMatchers {

  new play.core.StaticApplication(new java.io.File("."))

  it("process") {
    val org = Util.createOrganization()
    val meeting = Util.createMeeting(org)
    val user = Util.createUser()
    val team = Util.createTeam(org)
    TeamMembersDao.upsert(UsersDao.Default, TeamMemberForm(org, team.key, user.guid))
    Util.createSubscription(org, user, Publication.IncidentsTeamUpdate)
    val incident = Util.createIncident(
      org = org,
      form = Util.createIncidentForm().copy(teamKey = Some(team.key))
    )

    val email = MeetingAdjournedEmail(meeting.id).email
    email.subject should be(s"Meeting on ${DateHelper.mediumDateTime(org, meeting.scheduledAt)} has been adjourned")
    test.TestHelper.assertEqualsFile("test/resources/MeetingAdjournedEmailSpec.body.txt", email.body)
  }

}
