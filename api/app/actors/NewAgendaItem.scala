package actors

import com.gilt.quality.models.{AgendaItem, Meeting}
import db.{AgendaItemsDao, MeetingsDao}
import lib.Email
import core.mail.Person
import play.api.Play.current

object NewAgendaItem {

  val qualityHost = current.configuration.getString("quality.host").getOrElse {
    sys.error(s"configuration parameter[quality.host] is required")
  }

  private[actors] def newAgendaItem(agendaItemId: Long) {
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
    views.html.incidents.showForEmail(
      item.incident.organization,
      meeting,
      item,
      qualityHost
    ).toString

/*
    id: Long,
    organization: com.gilt.quality.models.Organization,
    summary: String,
    description: scala.Option[String] = None,
    team: scala.Option[com.gilt.quality.models.Team] = None,
    severity: com.gilt.quality.models.Severity,
    tags: scala.collection.Seq[String] = Nil,
    plan: scala.Option[com.gilt.quality.models.Plan] = None,
    createdAt: _root_.org.joda.time.DateTime

    Seq(
      "<ul>",
      s"  <li><a href='${url}'Incident ${item.incident.id}: ${item.incident.summary}></li>",
      "</ul>"
    ).mkString("\n")
 */
  }

}

