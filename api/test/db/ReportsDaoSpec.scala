package db

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class ReportsDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val report = Util.report()
      val fetched = ReportsDao.findById(report.id).get
      fetched.id should be(report.id)
      fetched.incident_id should be(report.incident_id)
      fetched.body should be(report.body)
    }
  }

  it("find by incident id") {
    running(FakeApplication()) {
      /*
      val teamKey = UUID.randomUUID.toString

      val i1 = ReportsDao.create(user, form.copy(team_key = teamKey))
      val i2 = ReportsDao.create(user, form.copy(team_key = teamKey))
      val other = ReportsDao.create(user, form)

      ReportsDao.findAll(teamKey = Some(teamKey)).map(_.id).sorted should be(Seq(i1.id, i2.id))
      ReportsDao.findAll(teamKey = Some(UUID.randomUUID.toString)).map(_.id) should be(Seq.empty)
       */
    }
  }

}
