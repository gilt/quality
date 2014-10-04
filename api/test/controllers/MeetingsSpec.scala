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

  "validates two meetings cannot be scheduled at the same time" in new WithServer {
    val scheduledAt = (new DateTime()).plus(3)
    val meeting = createMeeting(MeetingForm(scheduledAt = scheduledAt))
    intercept[ErrorsResponse] {
      createMeeting(MeetingForm(scheduledAt = scheduledAt))
    }.errors.map(_.message) must be(Seq("there is already a meeting scheduled at this time"))

  }

  "PUT /meetings/:id" in new WithServer {
    val scheduledAt = (new DateTime()).plus(3)
    val scheduledAt2 = scheduledAt.plus(1)
    val meeting = createMeeting(MeetingForm(scheduledAt = scheduledAt))
    meeting.scheduledAt must be(scheduledAt)

    val updatedMeeting = await(
      client.meetings.putById(
        id = meeting.id,
        meetingForm = MeetingForm(scheduledAt = scheduledAt2)
      )
    )
    updatedMeeting.scheduledAt must be(scheduledAt2)
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

}
