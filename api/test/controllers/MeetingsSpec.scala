package controllers

import actors.Database
import com.gilt.quality.v0.models.{AdjournForm, Meeting, MeetingForm}
import com.gilt.quality.v0.errors.ErrorsResponse
import java.util.UUID
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._

class MeetingsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  "POST /:org/meetings" in new WithServer {
    val scheduledAt = (new DateTime()).plus(3)
    val meeting = createMeeting(org, MeetingForm(scheduledAt = scheduledAt))
    meeting.scheduledAt must be(scheduledAt)
  }

  "DELETE /:org/meetings/:id" in new WithServer {
    val meeting = createMeeting(org)
    await(client.meetings.deleteByOrgAndId(org.key, meeting.id)) must be(Some(()))
    await(client.meetings.getByOrg(org.key, id = Some(meeting.id))) must be(Seq.empty)
  }

  "GET /:org/meetings" in new WithServer {
    val meeting1 = createMeeting(org)
    val meeting2 = createMeeting(org)

    await(client.meetings.getByOrg(org.key, id = Some(-1))) must be(Seq.empty)
    await(client.meetings.getByOrg(org.key, id = Some(meeting1.id))).head must be(meeting1)
    await(client.meetings.getByOrg(org.key, id = Some(meeting2.id))).head must be(meeting2)
  }

  "GET /:org/meetings/:id" in new WithServer {
    val meeting = createMeeting(org)
    await(client.meetings.getByOrgAndId(org.key, meeting.id)) must be(Some(meeting))
    await(client.meetings.getByOrgAndId(org.key, -1)) must be(None)
  }

  "GET /:org/meetings for an incident" in new WithServer {
    val meeting1 = createMeeting(org)
    val meeting2 = createMeeting(org)
    val incident = createIncident(org)

    Database.ensureOrganizationHasUpcomingMeetings(org)
    Database.assignIncident(incident.id)

    val meetings = await(client.meetings.getByOrg(org.key, incidentId = Some(incident.id)))
    meetings.size must be(1)
  }

  "GET /:org/meetings for an agenda item" in new WithServer {
    val meeting = createMeeting(org)
    Database.ensureOrganizationHasUpcomingMeetings(org)
    val item = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))

    await(client.meetings.getByOrg(org.key, agendaItemId = Some(item.id))).map(_.id) must be(Seq(meeting.id))
    await(client.meetings.getByOrg(org.key, agendaItemId = Some(-1))).map(_.id) must be(Seq.empty)
  }

  "GET /:org/meetings/:id/pager/:incident_id" in new WithServer {
    val meeting = createMeeting(org)
    Database.ensureOrganizationHasUpcomingMeetings(org)
    val item1 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))
    val item2 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))
    val item3 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))

    val pager1 = await(client.meetings.getPagerByOrgAndIdAndIncidentId(org.key, meeting.id, item1.incident.id))
    pager1.meeting must be(meeting)
    pager1.priorIncident.map(_.id) must be(None)
    pager1.nextIncident.map(_.id) must be(Some(item2.incident.id))

    val pager2 = await(client.meetings.getPagerByOrgAndIdAndIncidentId(org.key, meeting.id, item2.incident.id))
    pager2.meeting must be(meeting)
    pager2.priorIncident.map(_.id) must be(Some(item1.incident.id))
    pager2.nextIncident.map(_.id) must be(Some(item3.incident.id))

    val pager3 = await(client.meetings.getPagerByOrgAndIdAndIncidentId(org.key, meeting.id, item3.incident.id))
    pager3.meeting must be(meeting)
    pager3.priorIncident.map(_.id) must be(Some(item2.incident.id))
    pager3.nextIncident.map(_.id) must be(None)
  }

  "POST /:org/meetings/:id/adjourn" in new WithServer {
    val meeting = createMeeting(org)
    val updated = await(
      client.meetings.postAdjournByOrgAndId(
        org = org.key,
        id = meeting.id,
        adjournForm = AdjournForm()
      )
    )

    updated.adjournedAt.get
  }

  "POST /:org/meetings/:id/adjourn w/ explicit time" in new WithServer {
    val meeting = createMeeting(org)
    val now = new DateTime().plusWeeks(-1)
    val updated = await(
      client.meetings.postAdjournByOrgAndId(
        org = org.key,
        id = meeting.id,
        adjournForm = AdjournForm(adjournedAt = Some(now))
      )
    )

    updated.adjournedAt must be(Some(now))
  }

}
