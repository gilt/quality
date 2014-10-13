package controllers

import actors.Database
import com.gilt.quality.models.{Meeting, MeetingForm}
import com.gilt.quality.error.ErrorsResponse
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

}
