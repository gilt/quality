package lib

import client.Api
import com.gilt.quality.models.{Incident, Meeting, Organization}
import scala.concurrent.ExecutionContext.Implicits.global

case class IncidentContext(
  org: Organization,
  incident: Incident,
  meeting: Meeting
) {

  // private lazy val incidentIds = Api.instance.agendaItems.getMeetingsAndAgendaItemsByOrgAndMeetingId(org.key, meeting.id).flatMap(_.incident.id)

  lazy val previousId: Option[Long] = None
  lazy val nextId: Option[Long] = None

}
