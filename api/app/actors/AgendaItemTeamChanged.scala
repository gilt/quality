package actors

import com.gilt.quality.models.{AgendaItem, Meeting, Task}
import db.{AgendaItemsDao, MeetingsDao}
import lib.Email
import core.mail.Person
import play.api.Play.current

object AgendaItemTeamChanged {

  val qualityWebHostname = current.configuration.getString("quality.webHostname").getOrElse {
    sys.error(s"configuration parameter[quality.webHostname] is required")
  }

  private[actors] def processEvent(agendaItemId: Long) {
    AgendaItemsDao.findById(agendaItemId).map { item =>
      MeetingsDao.findAll(
        agendaItemId = Some(item.id),
        limit = 1
      ).headOption.map { meeting =>
        val teamEmail = "michael@gilt.com" // TODO
        Email.sendHtml(
          to = Person(teamEmail, item.incident.team.map(_.key)),
          subject = "[PerfectDay] Incident ${item.incident.id} scheduled to ${item.task} on ${meeting.scheduledAt}",
          body = itemToEmail(meeting, item)
        )
      }
    }
  }

  def itemToEmail(
    meeting: Meeting,
    item: AgendaItem
  ): String = {
    val taskLabel = item.task match {
      case Task.ReviewTeam => {
        "review team assignment"
      }
      case Task.ReviewPlan => {
        "review the prevention plan"
      }
      case Task.UNDEFINED(key) => {
        key
      }
    }

    views.html.emails.agendaItemTeamChanged(
      item.incident.organization,
      meeting,
      item,
      taskLabel,
      qualityWebHostname
    ).toString

  }

}

