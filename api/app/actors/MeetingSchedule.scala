package actors

import com.gilt.quality.models.Organization
import org.joda.time.DateTime

case class MeetingSchedule(
  dayOfWeek: String,
  beginningHourUTC: Int,
  beginningMinute: Int = 0
) {
  require(beginningHourUTC >= 0 && beginningHourUTC <= 23, s"Invalid beginningHourUTC[$beginningHourUTC]")
  require(beginningMinute >= 0 && beginningMinute <= 59, s"Invalid beginningMinute[$beginningMinute]")

  def upcomingDates(): Seq[DateTime] = {
    // TODO: Implement actual meeting times
    Seq(
      (new DateTime()).plus(MeetingSchedule.OneDay * 7),
      (new DateTime()).plus(MeetingSchedule.OneDay * 14)
    )
  }

}

object MeetingSchedule {

  val OneHour = 3600 * 1000l
  val OneDay = OneHour * 24

  // Eventually can move to configuration in database for each
  // organization
  private val OrgSchedule = Map(
    "gilt" -> MeetingSchedule("Thursday", 15)
  )

  def findByOrganization(
    org: Organization
  ): Option[MeetingSchedule] = {
    OrgSchedule.get(org.key)
  }
}

