package actors

import core.{DateHelper, Defaults}
import db.{AgendaItemsDao, MeetingsDao, Pager, TeamsDao}
import lib.{Email, Person}
import java.util.UUID
import com.gilt.quality.v0.models.{AgendaItem, EmailMessage, Incident, Meeting, Publication, Task, Team, User}
import play.api.Logger

case class MeetingIncidents(incidents: Seq[Incident]) {
  private val withGrade = incidents.filter(!_.plan.flatMap(_.grade).isEmpty)

  val withSmiley: Seq[Incident] = withGrade.filter(i => Defaults.isGoodGrade(i.plan.get.grade.get))
  val withFrowny: Seq[Incident] = withGrade.filter(i => !Defaults.isGoodGrade(i.plan.get.grade.get))
  val withNoGrade: Seq[Incident] = incidents.filter(_.plan.flatMap(_.grade).isEmpty)

  require(
    (withSmiley ++ withFrowny ++ withNoGrade).map(_.id).sorted == incidents.map(_.id).sorted
  )

}

case class MeetingAdjournedEmail(meetingId: Long) {

  private lazy val meeting = MeetingsDao.findById(meetingId)
  private lazy val allAgendaItems = allAgendaItemsForMeeting(meeting.get)

  /**
    * Generates the email message for this particular User.
    */
  def email(user: User) = meeting.map { m =>
    require(!m.adjournedAt.isEmpty, s"Meeting[${m.id}] must be adjourned")

    val userTeams = allTeamsForUser(user)
    val userTeamKeys = userTeams.map(_.key)

    EmailMessage(
      subject = s"Meeting on ${DateHelper.mediumDateTime(m.organization, m.scheduledAt)} has been adjourned",
      body = views.html.emails.meetingAdjourned(
        user,
        meeting.get,
        MeetingIncidents(allAgendaItems.filter(_.task == Task.ReviewPlan).map(_.incident)),
        allAgendaItems,
        userTeams,
        allAgendaItems.filter(item => !item.incident.team.isEmpty && userTeamKeys.contains(item.incident.team.get.key)),
        allAgendaItems.filter(item => item.incident.team.isEmpty || !userTeamKeys.contains(item.incident.team.get.key))
      ).toString
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
        userGuid = Some(user.guid),
        limit = 100,
        offset = offset
      )
    } { team =>
      teams.append(team)
    }

    teams
  }

  private def allAgendaItemsForMeeting(meeting: Meeting): Seq[AgendaItem] = {
    val agendaItems = scala.collection.mutable.ListBuffer[AgendaItem]()

    Pager.eachPage[AgendaItem] { offset =>
      AgendaItemsDao.findAll(
        meetingId = Some(meeting.id),
        limit = 100,
        offset = offset
      )
    } { agendaItem =>
      agendaItems.append(agendaItem)
    }

    agendaItems
  }

}
