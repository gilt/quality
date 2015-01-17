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
  beginningHour: Int,
  beginningMinute: Int = 0,
  timeZone: DateTimeZone
) {
  require(beginningHour >= 0 && beginningHour <= 23, s"Invalid beginningHour[$beginningHour]")
  require(beginningMinute >= 0 && beginningMinute <= 59, s"Invalid beginningMinute[$beginningMinute]")

  def upcomingDates(): Seq[DateTime] = {
    val now = new DateTime(timeZone).withTime(beginningHour, beginningMinute, 0, 0)
    val dow = now.weekOfWeekyear().roundFloorCopy().plusDays(dayOfWeek.jodaValue-1).withTime(beginningHour, beginningMinute, 0, 0)
    if (dow.isBefore(now.plusHours(12))) {
      Seq(dow.plusWeeks(1), dow.plusWeeks(2))
    } else {
      Seq(dow, dow.plusWeeks(1))
    }
  }

}

object MeetingSchedule {

  val DefaultMeetingSchedule = MeetingSchedule.newYork(
    dayOfWeek = DayOfWeek.Thursday,
    beginningHour = 11
  )

  def newYork(
    dayOfWeek: DayOfWeek,
    beginningHour: Int,
    beginningMinute: Int = 0
  ): MeetingSchedule = {
    MeetingSchedule(
      dayOfWeek = dayOfWeek,
      beginningHour = beginningHour,
      beginningMinute = beginningMinute,
      timeZone = DateTimeZone.forID("America/New_York")
    )
  }

  def findByOrganization(org: Organization): Option[MeetingSchedule] = {
    Some(DefaultMeetingSchedule)
  }
}

