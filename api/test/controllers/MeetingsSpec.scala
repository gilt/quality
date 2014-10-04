package controllers

import com.gilt.quality.models.{Meeting, MeetingForm}
import com.gilt.quality.error.ErrorsResponse
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._

class MeetingsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "create a meeting" in new WithServer {
    val scheduledAt = (new DateTime()).plus(3)
    val meeting = createMeeting(MeetingForm(scheduledAt = scheduledAt))
    meeting.scheduledAt must be(scheduledAt)
  }

  "DELETE /meetings/:id" in new WithServer {
    val meeting = createMeeting()
    await(client.meetings.deleteById(meeting.id)) must be(Some(()))
    await(client.meetings.get(id = Some(meeting.id))) must be(Seq.empty)
  }

  "GET /meetings" in new WithServer {
    val meeting1 = createMeeting()
    val meeting2 = createMeeting()

    await(client.meetings.get(id = Some(-1))) must be(Seq.empty)
    await(client.meetings.get(id = Some(meeting1.id))).head must be(meeting1)
    await(client.meetings.get(id = Some(meeting2.id))).head must be(meeting2)
  }

  "GET /meetings/:id" in new WithServer {
    val meeting = createMeeting()
    await(client.meetings.getById(meeting.id)) must be(Some(meeting))
    await(client.meetings.getById(-1)) must be(None)
  }

}
