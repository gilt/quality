package actors

import org.joda.time.{DateTime, DateTimeConstants, DateTimeZone}

import java.util.UUID
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class MeetingScheduleSpec extends FunSpec with ShouldMatchers {

  it("upcomingDates on thursdays at noon") {
    val now = new DateTime()
    val nextDates = MeetingSchedule.newYork(DayOfWeek.Thursday, 12, 0).upcomingDates
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
    val nextDates = MeetingSchedule.newYork(DayOfWeek.Tuesday, 15, 14).upcomingDates
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
    MeetingSchedule.newYork(DayOfWeek.Thursday, 12, 0).beginningMinute should be(0)
    MeetingSchedule.newYork(DayOfWeek.Thursday, 12, 59).beginningMinute should be(59)

    intercept[IllegalArgumentException] {
      MeetingSchedule.newYork(DayOfWeek.Thursday, 12, 60)
    }.getMessage should be("requirement failed: Invalid beginningMinute[60]")

    intercept[IllegalArgumentException] {
      MeetingSchedule.newYork(DayOfWeek.Thursday, 12, -1)
    }.getMessage should be("requirement failed: Invalid beginningMinute[-1]")
  }

  it("validates hour") {
    MeetingSchedule.newYork(DayOfWeek.Thursday, 0).beginningHour should be(0)
    intercept[IllegalArgumentException] {
      MeetingSchedule.newYork(DayOfWeek.Thursday, 24)
    }.getMessage should be("requirement failed: Invalid beginningHour[24]")

    intercept[IllegalArgumentException] {
      MeetingSchedule.newYork(DayOfWeek.Thursday, -1)
    }.getMessage should be("requirement failed: Invalid beginningHour[-1]")
  }

}
