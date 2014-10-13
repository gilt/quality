package actors

import com.gilt.quality.models.OrganizationForm
import db.{OrganizationsDao, User, Util}
import org.joda.time.{DateTime, DateTimeConstants}

import java.util.UUID
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class MeetingScheduleSpec extends FunSpec with ShouldMatchers {

  it("upcomingDates on thursdays at noon") {
    val now = new DateTime()
    val nextDates = MeetingSchedule(DayOfWeek.Thursday, 12, 0).upcomingDates
    nextDates.size should be(2)
    nextDates.find(_.isBefore(now)) should be(None)
    nextDates.foreach { d =>
      d.getDayOfWeek() should be(DateTimeConstants.THURSDAY)
      d.getHourOfDay() should be(12)
      d.getMinuteOfHour() should be(0)
      d.getSecondOfMinute() should be(0)
    }
  }

  it("upcomingDates on tuesdays at 3:14 pm") {
    val now = new DateTime()
    val nextDates = MeetingSchedule(DayOfWeek.Tuesday, 15, 14).upcomingDates
    nextDates.size should be(2)
    nextDates.find(_.isBefore(now)) should be(None)

    nextDates.foreach { d =>
      d.getDayOfWeek() should be(DateTimeConstants.TUESDAY)
      d.getHourOfDay() should be(15)
      d.getMinuteOfHour() should be(14)
      d.getSecondOfMinute() should be(0)
    }
  }

  it("validates minute") {
    MeetingSchedule(DayOfWeek.Thursday, 12, 0).beginningMinute should be(0)
    MeetingSchedule(DayOfWeek.Thursday, 12, 59).beginningMinute should be(59)

    intercept[IllegalArgumentException] {
      MeetingSchedule(DayOfWeek.Thursday, 12, 60)
    }.getMessage should be("requirement failed: Invalid beginningMinute[60]")

    intercept[IllegalArgumentException] {
      MeetingSchedule(DayOfWeek.Thursday, 12, -1)
    }.getMessage should be("requirement failed: Invalid beginningMinute[-1]")
  }

  it("validates hour") {
    MeetingSchedule(DayOfWeek.Thursday, 0).beginningHourUTC should be(0)
    intercept[IllegalArgumentException] {
      MeetingSchedule(DayOfWeek.Thursday, 24)
    }.getMessage should be("requirement failed: Invalid beginningHourUTC[24]")

    intercept[IllegalArgumentException] {
      MeetingSchedule(DayOfWeek.Thursday, -1)
    }.getMessage should be("requirement failed: Invalid beginningHourUTC[-1]")
  }

  it("orgs have meeting schedules") {
    val org = Util.createOrganization()
    MeetingSchedule.findByOrganization(org) should be(Some(MeetingSchedule.DefaultMeetingSchedule))
  }
}
