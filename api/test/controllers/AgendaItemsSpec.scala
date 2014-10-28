package controllers

import com.gilt.quality.FailedRequest
import com.gilt.quality.models.{AdjournForm, AgendaItem, AgendaItemForm, Task, Team}
import com.gilt.quality.error.ErrorsResponse
import org.joda.time.DateTime
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class AgendaItemsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()

  "GET /agenda_items by userGuid" in new WithServer {
    val team = createTeam(org)
    val user1 = createUser()
    val user2 = createUser()
    val item = createAgendaItemForTeam(team)

    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), userGuid = Some(UUID.randomUUID))) must be(Seq.empty)
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), userGuid = Some(user1.guid))) must be(Seq.empty)
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), userGuid = Some(user2.guid))) must be(Seq.empty)

    await(client.teams.putMembersByOrgAndKeyAndUserGuid(org.key, team.key, user1.guid))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), userGuid = Some(user1.guid))).map(_.id) must be(Seq(item.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), userGuid = Some(user2.guid))) must be(Seq.empty)
  }

  "GET /agenda_items by teamKey" in new WithServer {
    val team = createTeam(org)
    val otherTeam = createTeam(org)
    val item = createAgendaItemForTeam(team)

    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), teamKey = Some(UUID.randomUUID.toString))) must be(Seq.empty)
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), teamKey = Some(otherTeam.key))) must be(Seq.empty)
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), teamKey = Some(team.key))).map(_.id) must be(Seq(item.id))
  }

  "GET /agenda_items by isAdjourned" in new WithServer {
    val item = createAgendaItem(org)

    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id))).map(_.id) must be(Seq(item.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), isAdjourned = Some(false))).map(_.id) must be(Seq(item.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), isAdjourned = Some(true))).map(_.id) must be(Seq.empty)

    await(
      client.meetings.postAdjournByOrgAndId(
        org = org.key,
        id = item.meeting.id,
        adjournForm = AdjournForm()
      )
    )

    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id))).map(_.id) must be(Seq(item.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), isAdjourned = Some(false))).map(_.id) must be(Seq.empty)
    await(client.agendaItems.getAgendaItemsByOrg(org.key, id = Some(item.id), isAdjourned = Some(true))).map(_.id) must be(Seq(item.id))
  }

  "POST /:org/agenda_items" in new WithServer {
    val meeting = createMeeting(org)
    val incident = createIncident(org)
    val item = createAgendaItem(
      org = org,
      form = Some(
        AgendaItemForm(
          meetingId = meeting.id,
          incidentId = incident.id,
          task = Task.ReviewTeam
        )
      )
    )

    item.incident.id must be(incident.id)
    item.task must be(Task.ReviewTeam)
  }

  "POST /:org/agenda_items validates task" in new WithServer {
    val meeting = createMeeting(org)
    val incident = createIncident(org)

    intercept[ErrorsResponse] {
      createAgendaItem(
        org = org,
        form = Some(
          AgendaItemForm(
            meetingId = meeting.id,
            incidentId = incident.id,
            task = Task.UNDEFINED("foo")
          )
        )
      )
    }.errors.map(_.message) must be (Seq("Invalid task[foo]"))
  }

  "GET /agenda_items filters by meeting, agenda item" in new WithServer {
    val org = createOrganization()
    val meeting = createMeeting(org)
    val item1 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))
    val item2 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))

    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(0))) must be(Seq.empty)
    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id))).map(_.id).sorted must be(Seq(item1.id, item2.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id), id = Some(item1.id))).map(_.id) must be(Seq(item1.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id), id = Some(item2.id))).map(_.id) must be(Seq(item2.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id), id = Some(-1))).map(_.id) must be(Seq.empty)
  }

  "GET /agenda_items is 404 if org not found" in new WithServer {
    intercept[FailedRequest] {
      await(client.agendaItems.getAgendaItemsByOrg(UUID.randomUUID.toString))
    }.response.status must be(404)
  }

  "GET /agenda_items paginates" in new WithServer {
    val org = createOrganization()
    val meeting = createMeeting(org)
    val item1 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))
    val item2 = createAgendaItem(org, Some(createAgendaItemForm(org, meeting = Some(meeting))))

    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id), limit = Some(1))).map(_.id) must be(Seq(item1.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id), limit = Some(1), offset = Some(1))).map(_.id) must be(Seq(item2.id))
    await(client.agendaItems.getAgendaItemsByOrg(org.key, meetingId = Some(meeting.id), limit = Some(1), offset = Some(2))) must be(Seq.empty)
  }

  "GET /agenda_items/:id" in new WithServer {
    val item = createAgendaItem(org)

    await(client.agendaItems.getAgendaItemsByOrgAndId(org.key, item.id)).map(_.id) must be(Some(item.id))
    await(client.agendaItems.getAgendaItemsByOrgAndId(org.key, -1)) must be(None)
  }

  "DELETE /agenda_items/:id" in new WithServer {
    val item = createAgendaItem(org)

    await(client.agendaItems.getAgendaItemsByOrgAndId(org.key, item.id)).map(_.id) must be(Some(item.id))
    await(client.agendaItems.deleteAgendaItemsByOrgAndId(org.key, item.id))
    await(client.agendaItems.getAgendaItemsByOrgAndId(org.key, item.id)) must be(None)
  }

}
