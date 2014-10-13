package db

import com.gilt.quality.models.Task

import org.joda.time.DateTime
import org.scalatest.{FunSpec, Matchers}
import org.junit.Assert._
import java.util.UUID

class MeetingsDaoSpec extends FunSpec with Matchers {

  it("upsert") {
    val org = Util.createOrganization()
    val date = new DateTime()
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)) should be(Seq.empty)

    val mtg = MeetingsDao.upsert(org, date)
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)).map(_.id) should be(Seq(mtg.id))

    MeetingsDao.upsert(org, date)
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)).map(_.id) should be(Seq(mtg.id))

    MeetingsDao.softDelete(User.Default, mtg)
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)) should be(Seq.empty)

    val mtg2 = MeetingsDao.upsert(org, date)
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)).map(_.id) should be(Seq(mtg2.id))
    mtg.id shouldNot be(mtg2.id)
    mtg.scheduledAt should be(mtg2.scheduledAt)
  }

  it("upsertAgendaItem") {
    val org = Util.createOrganization()
    val meeting = Util.createMeeting(org)
    val incident = Util.createIncident(org)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq.empty)

    val item = MeetingsDao.upsertAgendaItem(meeting, incident, Task.ReviewTeam)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq(item.id))

    MeetingsDao.upsertAgendaItem(meeting, incident, Task.ReviewTeam)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq(item.id))

    AgendaItemsDao.softDelete(User.Default, item)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq.empty)

    val item2 = MeetingsDao.upsertAgendaItem(meeting, incident, Task.ReviewTeam)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq(item2.id))

    item.id shouldNot be(item2.id)
    item.task should be(item2.task)
    item.incident.id should be(item2.incident.id)
  }

}
