package actors

import com.gilt.quality.models.{AgendaItem, Meeting, Task}
import core.DateHelper
import db.{AgendaItemsDao, MeetingsDao}
import lib.{Email, Person}
import play.api.Play.current

object AgendaItemEvents {

  private[actors] def processCreated(agendaItemId: Long) {
    println(s"processCreated($agendaItemId)")
    AgendaItemsDao.findById(agendaItemId).map { item =>
      MeetingsDao.findAll(
        agendaItemId = Some(item.id),
        limit = 1
      ).headOption.map { meeting =>
        val dateTime = DateHelper.shortDate(item.incident.organization, meeting.scheduledAt)
        val emails = Seq(
          Some("michael@gilt.com"),
          Some("chazlett@gilt.com"),
          item.incident.team.flatMap(_.email)
        ).flatten.foreach { teamEmail =>
          Email.sendHtml(
            to = Person(teamEmail, item.incident.team.map(_.key)),
            subject = s"Incident ${item.incident.id} Added to ${dateTime} Meeting to ${Emails.taskLabel(item.task)}",
            body = views.html.emails.agendaItemTeamChanged(
              Emails.qualityWebHostname,
              item.incident.organization,
              meeting,
              item,
              Emails.taskLabel(item.task)
            ).toString
          )
        }
      }
    }
  }

}

