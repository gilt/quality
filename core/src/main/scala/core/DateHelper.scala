package core

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import com.gilt.quality.models.Organization

object DateHelper {

  private[this] case class OrgTimeZone(
    name: String,
    label: String
  )

  def shortDate(
    org: Organization,
    dateTime: DateTime
  ): String = {
    val tz = orgTimeZone(org)
    DateTimeFormat.shortDate.withZone(DateTimeZone.forID(tz.name)).print(dateTime) + s" ${tz.label}"
  }

  def mediumDateTime(
    org: Organization,
    dateTime: DateTime
  ): String = {
    val tz = orgTimeZone(org)
    DateTimeFormat.mediumDateTime.withZone(DateTimeZone.forID(tz.name)).print(dateTime) + s" ${tz.label}"
  }

  def longDateTime(
    org: Organization,
    dateTime: DateTime
  ): String = {
    val tz = orgTimeZone(org)
    DateTimeFormat.longDateTime.withZone(DateTimeZone.forID(tz.name)).print(dateTime) + s" ${tz.label}"
  }

  // Place holder to enable time zone configuration later by
  // organization. Strings returned must be valid for
  // java.util.TimeZone - see
  // http://joda-time.sourceforge.net/timezones.html
  private def orgTimeZone(org: Organization) = OrgTimeZone(name = "US/Eastern", label = "EST")

}
