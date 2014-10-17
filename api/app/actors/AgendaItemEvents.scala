package actors

import com.gilt.quality.models.{AgendaItem, Meeting, Task}
import db.{AgendaItemsDao, MeetingsDao}
import lib.Email
import core.mail.Person
import play.api.Play.current

object AgendaItemEvents {

  val qualityWebHostname = current.configuration.getString("quality.webHostname").getOrElse {
    sys.error(s"configuration parameter[quality.webHostname] is required")
  }

  private[actors] def processCreated(agendaItemId: Long) {
    AgendaItemsDao.findById(agendaItemId).map { item =>
      MeetingsDao.findAll(
        agendaItemId = Some(item.id),
        limit = 1
      ).headOption.map { meeting =>
        // TODO
        Seq("michael@gilt.com", "chazlett@gilt.com").foreach { teamEmail =>
          val dateTime = org.joda.time.format.DateTimeFormat.shortDate.print(meeting.scheduledAt)
          Email.sendHtml(
            to = Person(teamEmail, item.incident.team.map(_.key)),
            subject = s"[PerfectDay] Incident ${item.incident.id} Added to ${dateTime} Meeting to ${taskLabel(item.task)}",
            body = itemToEmail(meeting, item)
          )
        }
      }
    }
  }

  def itemToEmail(
    meeting: Meeting,
    item: AgendaItem
  ): String = {
    views.html.emails.agendaItemTeamChanged(
      item.incident.organization,
      meeting,
      item,
      taskLabel(item.task),
      qualityWebHostname
    ).toString

  }

  private def taskLabel(task: Task): String = {
    task match {
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
  }


}

