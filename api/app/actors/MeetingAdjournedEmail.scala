package actors

import com.gilt.quality.models.Meeting
import db.MeetingsDao

object MeetingAdjournedEmail {

  def process(meetingId: Long) {
    MeetingsDao.findById(meetingId).map { meeting =>
      require(!meeting.adjournedAt.isEmpty, s"Meeting[${meeting.id}] must be adjourned")
    }
  }

}
