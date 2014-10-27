package actors

import core.DateHelper
import db.{MeetingsDao, Pager, TeamsDao}
import lib.{Email, Person}
import java.util.UUID
import com.gilt.quality.models.{EmailMessage, Meeting, Publication, Team, User}
import play.api.Logger

case class MeetingAdjournedEmail(meetingId: Long) {

  private lazy val meeting = MeetingsDao.findById(meetingId)

  /**
    * Generates the email message for this particular User.
    */
  def email(user: User) = meeting.map { m =>
    require(!m.adjournedAt.isEmpty, s"Meeting[${m.id}] must be adjourned")

    EmailMessage(
      subject = s"Meeting on ${DateHelper.mediumDateTime(m.organization, m.scheduledAt)} has been adjourned",
      body = views.html.emails.meetingAdjourned(user, meeting.get).toString
    )
  }.getOrElse {
    sys.error(s"Meeting $meetingId not found")
  }

  def send() {
    meeting.map { m =>
      Emails.eachSubscription(m.organization, Publication.MeetingsAdjourned, None, { subscription =>
        Logger.info(s"Emails: delivering email for subscription[$subscription]")

        val msg = email(subscription.user)
        Email.sendHtml(
          to = Person(email = subscription.user.email),
          subject = msg.subject,
          body = msg.body
        )
      })
    }
  }

  private def allTeamsForUser(user: User): Seq[Team] = {
    val teams = scala.collection.mutable.ListBuffer[Team]()

    Pager.eachPage[Team] { offset =>
      TeamsDao.findAll(
        org = meeting.get.organization,
        memberUserGuid = Some(user.guid),
        limit = 100
      )
    } { team =>
      teams.append(team)
    }

    teams
  }

}
