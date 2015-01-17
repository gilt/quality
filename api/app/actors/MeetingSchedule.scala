package actors

import com.gilt.quality.v0.models.Organization
import org.joda.time.{DateTime, DateTimeConstants, DateTimeZone}

sealed trait DayOfWeek {
  def jodaValue: Int
}

object DayOfWeek {
  case object Sunday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.SUNDAY // 7
    override def toString = "Sunday"
  }
  case object Monday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.MONDAY // 1
    override def toString = "Monday"
  }
  case object Tuesday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.TUESDAY
    override def toString = "Tuesday"
  }
  case object Wednesday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.WEDNESDAY
    override def toString = "Wednesday"
  }
  case object Thursday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.THURSDAY
    override def toString = "Thursday"
  }
  case object Friday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.FRIDAY
    override def toString = "Friday"
  }
  case object Saturday extends DayOfWeek {
    override def jodaValue = DateTimeConstants.SATURDAY
    override def toString = "Saturday"
  }
}

case class MeetingSchedule(
  dayOfWeek: DayOfWeek,
  beginningHourUTC: Int,
  beginningMinute: Int = 0
) {
  require(beginningHourUTC >= 0 && beginningHourUTC <= 23, s"Invalid beginningHourUTC[$beginningHourUTC]")
  require(beginningMinute >= 0 && beginningMinute <= 59, s"Invalid beginningMinute[$beginningMinute]")

  def upcomingDates(): Seq[DateTime] = {
    val now = new DateTime(DateTimeZone.UTC).withTime(beginningHourUTC, beginningMinute, 0, 0)
    val dow = now.weekOfWeekyear().roundFloorCopy().plusDays(dayOfWeek.jodaValue-1).withTime(beginningHourUTC, beginningMinute, 0, 0)
    if (dow.isBefore(now.plusHours(12))) {
      Seq(dow.plusWeeks(1), dow.plusWeeks(2))
    } else {
      Seq(dow, dow.plusWeeks(1))
    }
  }

}

object MeetingSchedule {

  val DefaultMeetingSchedule = MeetingSchedule(DayOfWeek.Thursday, 15)

  def findByOrganization(org: Organization): Option[MeetingSchedule] = {
    Some(DefaultMeetingSchedule)
  }
}

