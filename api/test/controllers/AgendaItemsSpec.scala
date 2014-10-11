package controllers

import com.gilt.quality.models.{AgendaItem, AgendaItemForm, Task}
import com.gilt.quality.error.ErrorsResponse
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._

class AgendaItemsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "POST /meetings/:meeting_id/agenda_items" in new WithServer {
    val meeting = createMeeting()
    val incident = createIncident()
    val item = createAgendaItem(
      meeting = meeting,
      form = AgendaItemForm(
        incidentId = incident.id,
        task = Task.ReviewTeam
      )
    )

    item.incident.id must be(incident.id)
    item.task must be(Task.ReviewTeam)
  }

  "POST /meetings/:meeting_id/agenda_items validates task" in new WithServer {
    val meeting = createMeeting()
    val incident = createIncident()

    intercept[ErrorsResponse] {
      createAgendaItem(
        meeting = meeting,
        form = AgendaItemForm(
          incidentId = incident.id,
          task = Task.UNDEFINED("foo")
        )
      )
    }.errors.map(_.message) must be (Seq("Invalid task[foo]"))
  }

  "GET /meetings/:meeting_id/agenda_items filters by meeting, agenda item" in new WithServer {
    val meeting = createMeeting()
    val item1 = createAgendaItem(meeting = meeting)
    val item2 = createAgendaItem(meeting = meeting)

    await(client.agendaItems.get(meetingId = meeting.id)).map(_.id).sorted must be(Seq(item1.id, item2.id))
    await(client.agendaItems.get(meetingId = meeting.id, id = Some(item1.id))).map(_.id) must be(Seq(item1.id))
    await(client.agendaItems.get(meetingId = meeting.id, id = Some(item2.id))).map(_.id) must be(Seq(item2.id))
    await(client.agendaItems.get(meetingId = meeting.id, id = Some(-1))).map(_.id) must be(Seq.empty)
    await(client.agendaItems.get(meetingId = -1, id = Some(item2.id))).map(_.id) must be(Seq.empty)
  }

  "GET /meetings/:meeting_id/agenda_items paginates" in new WithServer {
    val meeting = createMeeting()
    val item1 = createAgendaItem(meeting = meeting)
    val item2 = createAgendaItem(meeting = meeting)

    await(client.agendaItems.get(meetingId = meeting.id, limit = Some(1))).map(_.id).sorted must be(Seq(item1.id))
    await(client.agendaItems.get(meetingId = meeting.id, limit = Some(1), offset = Some(1))).map(_.id).sorted must be(Seq(item2.id))
    await(client.agendaItems.get(meetingId = meeting.id, limit = Some(1), offset = Some(2))).map(_.id).sorted must be(Seq.empty)
  }

  "GET /meetings/:meeting_id/agenda_items/:id" in new WithServer {
    val meeting = createMeeting()
    val item = createAgendaItem(meeting = meeting)

    await(client.agendaItems.getById(meeting.id, item.id)).map(_.id) must be(Some(item.id))
    await(client.agendaItems.getById(meeting.id, -1)) must be(None)
  }

  "DELETE /meetings/:meeting_id/agenda_items/:id" in new WithServer {
    val meeting = createMeeting()
    val item = createAgendaItem(meeting = meeting)

    await(client.agendaItems.getById(meeting.id, item.id)).map(_.id) must be(Some(item.id))
    await(client.agendaItems.deleteById(meeting.id, item.id)) must be(Some(()))
    await(client.agendaItems.getById(meeting.id, item.id)) must be(None)
  }

}
