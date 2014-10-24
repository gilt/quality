package actors

import com.gilt.quality.models.Meeting
import db.MeetingsDao

case class MeetingAdjournedEmail(meetingId: Long) {

  private lazy val meeting = MeetingsDao.findById(meetingId)

  lazy val email = {
    EmailMessage(
      subject = "test",
      body = "test"
    )
  }

  def send() {
    meeting.map { m =>
      require(!m.adjournedAt.isEmpty, s"Meeting[${m.id}] must be adjourned")
      println("EMAIL: " + email)
    }
  }

}
