package db

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

  }

}
