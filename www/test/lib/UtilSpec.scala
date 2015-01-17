package lib

import org.joda.time.DateTime
import org.scalatest.{FunSpec, Matchers}
import com.gilt.quality.v0.models.{Meeting, Organization}

class UtilSpec extends FunSpec with Matchers {

  val org = Organization(key = "gilt", name = "Gilt")

  def createMeeting(
    org: Organization,
    scheduledAt: DateTime = DateTime.now.plusHours(1),
    adjournedAt: Option[DateTime] = None
  ) = Meeting(
    id = 1l,
    organization = org,
    scheduledAt = scheduledAt,
    adjournedAt = adjournedAt
  )

  it("isAroundNow") {
    val now = DateTime.now
    Util.isAroundNow(now) should be(true)
    Util.isAroundNow(now.minusMinutes(59)) should be(true)
    Util.isAroundNow(now.minusMinutes(61)) should be(false)
    Util.isAroundNow(now.plusHours(1)) should be(true)
    Util.isAroundNow(now.plusHours(20)) should be(false)
  }

  it("meetingStatus") {
    Util.meetingStatus(createMeeting(org, adjournedAt = Some(DateTime.now))) should be("Adjourned")
    Util.meetingStatus(createMeeting(org)) should be("Scheduled")
    Util.meetingStatus(createMeeting(org, scheduledAt = DateTime.now.minusSeconds(1))) should be("In Progress")
  }

  it("meetingLabel") {
    Util.meetingLabel(createMeeting(org, adjournedAt = Some(DateTime.now))) should be(None)
    Util.meetingLabel(createMeeting(org, scheduledAt = DateTime.now.minusMinutes(6))) should be(None)
    Util.meetingLabel(createMeeting(org, scheduledAt = DateTime.now.minusMinutes(1))) should be(Some("Starting Soon"))
    Util.meetingLabel(createMeeting(org, scheduledAt = DateTime.now)) should be(Some("Starting Now"))
    Util.meetingLabel(createMeeting(org, scheduledAt = DateTime.now.plusMinutes(30))) should be(Some("Live"))
    Util.meetingLabel(createMeeting(org, scheduledAt = DateTime.now.plusMinutes(90))) should be(None)
  }

}
