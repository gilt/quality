package actors

import com.gilt.quality.models._
import core.DateHelper
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import db._
import java.util.UUID
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class MeetingAdjournedEmailSpec extends FunSpec with ShouldMatchers {

  new play.core.StaticApplication(new java.io.File("."))

  val org = OrganizationsDao.findByKey("meeting-adjourned-email-spec").getOrElse {
    OrganizationsDao.create(UsersDao.Default,
      OrganizationForm(
        name = "meeting-adjourned-email-spec",
        key = Some("meeting-adjourned-email-spec")
        )
    )
  }

  it("process") {
    val dateTime = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss").parseDateTime("10/23/2014 15:00:00")
    val meeting = MeetingsDao.upsert(org, dateTime)
    meeting.adjournedAt.getOrElse {
      MeetingsDao.adjourn(UsersDao.Default, meeting, AdjournForm(adjournedAt = Some(dateTime.plusWeeks(1))))
    }

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


    // TODO: Figure out best way to test this as data changes (like the meeting id)
    // test.TestHelper.assertEqualsFile("test/resources/MeetingAdjournedEmailSpec.body.txt", email.body)
  }

}
