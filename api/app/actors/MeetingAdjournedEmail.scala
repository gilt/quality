package actors

import core.DateHelper
import db.MeetingsDao
import lib.{Email, Person}
import com.gilt.quality.models.{Meeting, Publication}
import play.api.Logger

case class MeetingAdjournedEmail(meetingId: Long) {

  private lazy val meeting = MeetingsDao.findById(meetingId)

  lazy val email = meeting.map { m =>
    EmailMessage(
      subject = s"Meeting on ${DateHelper.mediumDateTime(m.organization, m.scheduledAt)} has been adjourned",
      body = views.html.emails.meetingAdjourned(meeting.get).toString
    )
  }.getOrElse {
    sys.error(s"Meeting $meetingId not found")
  }

  def send() {
    meeting.map { m =>
      require(!m.adjournedAt.isEmpty, s"Meeting[${m.id}] must be adjourned")

      Emails.eachSubscription(m.organization, Publication.MeetingsAdjourned, None, { subscription =>
        Logger.info(s"Emails: delivering email for subscription[$subscription]")
        Email.sendHtml(
          to = Person(email = subscription.user.email),
          subject = email.subject,
          body = email.body
        )
      })
    }
  }

}
