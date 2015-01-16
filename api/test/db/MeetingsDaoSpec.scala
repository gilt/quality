package db

import com.gilt.quality.v0.models.{AdjournForm, Task}

import org.joda.time.DateTime
import org.scalatest.{FunSpec, Matchers}
import org.junit.Assert._
import java.util.UUID

class MeetingsDaoSpec extends FunSpec with Matchers {

  new play.core.StaticApplication(new java.io.File("."))

  it("upsert") {
    val org = Util.createOrganization()
    val date = new DateTime()
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)) should be(Seq.empty)

    val mtg = MeetingsDao.upsert(org, date)
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)).map(_.id) should be(Seq(mtg.id))

    MeetingsDao.upsert(org, date)
    MeetingsDao.findAll(org = Some(org), scheduledAt = Some(date)).map(_.id) should be(Seq(mtg.id))

    MeetingsDao.softDelete(UsersDao.Default, mtg)
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

    AgendaItemsDao.softDelete(UsersDao.Default, item)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq.empty)

    val item2 = MeetingsDao.upsertAgendaItem(meeting, incident, Task.ReviewTeam)
    AgendaItemsDao.findAll(meetingId = Some(meeting.id)).map(_.id) should be(Seq(item2.id))

    item.id shouldNot be(item2.id)
    item.task should be(item2.task)
    item.incident.id should be(item2.incident.id)
  }

  it("findAll scheduledWithinNHours") {
    val org = Util.createOrganization()
    val now = new DateTime()

    val meeting13HoursAgo = MeetingsDao.upsert(org, now.plusHours(-13))
    val meeting11HoursAgo = MeetingsDao.upsert(org, now.plusHours(-11))
    val meetingNow = MeetingsDao.upsert(org, now)
    val meeting11HoursFromNow = MeetingsDao.upsert(org, now.plusHours(11))
    val meeting13HoursFromNow = MeetingsDao.upsert(org, now.plusHours(13))

    MeetingsDao.findAll(
      org = Some(org),
      scheduledWithinNHours = Some(14)
    ).map(_.id).sorted should be(Seq(meeting13HoursAgo.id, meeting11HoursAgo.id, meetingNow.id, meeting11HoursFromNow.id, meeting13HoursFromNow.id))

    MeetingsDao.findAll(
      org = Some(org),
      scheduledWithinNHours = Some(12)
    ).map(_.id).sorted should be(Seq(meeting11HoursAgo.id, meetingNow.id, meeting11HoursFromNow.id))

    MeetingsDao.findAll(
      org = Some(org),
      scheduledWithinNHours = Some(1)
    ).map(_.id).sorted should be(Seq(meetingNow.id))
  }

  it("findAll isUpcoming") {
    val org = Util.createOrganization()
    val now = new DateTime()
    val meetingPast = MeetingsDao.upsert(org, now.plusHours(-1))
    val meetingFuture = MeetingsDao.upsert(org, now.plusHours(1))

    MeetingsDao.findAll(Some(org)).map(_.id).sorted should be(Seq(meetingPast.id, meetingFuture.id))
    MeetingsDao.findAll(Some(org), isUpcoming = Some(true)).map(_.id).sorted should be(Seq(meetingFuture.id))
    MeetingsDao.findAll(Some(org), isUpcoming = Some(false)).map(_.id).sorted should be(Seq(meetingPast.id))
  }

  it("findAll isAdjourned") {
    val org = Util.createOrganization()
    val now = new DateTime()
    val meeting = MeetingsDao.upsert(org, now.plusHours(-1))
    val adjourned = MeetingsDao.adjourn(
      UsersDao.Default,
      MeetingsDao.upsert(org, now.plusHours(1)),
      AdjournForm()
    )

    MeetingsDao.findAll(Some(org)).map(_.id).sorted should be(Seq(meeting.id, adjourned.id))
    MeetingsDao.findAll(Some(org), isAdjourned = Some(true)).map(_.id).sorted should be(Seq(adjourned.id))
    MeetingsDao.findAll(Some(org), isAdjourned = Some(false)).map(_.id).sorted should be(Seq(meeting.id))
  }


}
