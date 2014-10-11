package actors

import com.gilt.quality.models.OrganizationForm
import db.{OrganizationsDao, User}
import org.joda.time.DateTime

import java.util.UUID
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class MeetingScheduleSpec extends FunSpec with ShouldMatchers {
/*
  it("orgs have meeting schedules") {
    running(FakeApplication()) {
      val org = OrganizationsDao.create(User.Default, OrganizationForm(name = UUID.randomUUID.toString))
      MeetingSchedule.findByOrganization(org) should be(Some(MeetingSchedule.DefaultMeetingSchedule))
    }
  }
 */

  it("upcomingDates") {
    val now = new DateTime()
    val nextDates = MeetingSchedule("Thursday", 12, 0).upcomingDates
    nextDates.size should be(2)
    nextDates.find(_.isBefore(now)) should be(None)
  }

  it("validates minute") {
    MeetingSchedule("Thursday", 12, 0).beginningMinute should be(0)
    MeetingSchedule("Thursday", 12, 59).beginningMinute should be(59)

    intercept[IllegalArgumentException] {
      MeetingSchedule("Thursday", 12, 60)
    }.getMessage should be("requirement failed: Invalid beginningMinute[60]")

    intercept[IllegalArgumentException] {
      MeetingSchedule("Thursday", 12, -1)
    }.getMessage should be("requirement failed: Invalid beginningMinute[-1]")
  }

  it("validates hour") {
    MeetingSchedule("Thursday", 0).beginningHourUTC should be(0)
    intercept[IllegalArgumentException] {
      MeetingSchedule("Thursday", 24)
    }.getMessage should be("requirement failed: Invalid beginningHourUTC[24]")

    intercept[IllegalArgumentException] {
      MeetingSchedule("Thursday", -1)
    }.getMessage should be("requirement failed: Invalid beginningHourUTC[-1]")
  }

}
