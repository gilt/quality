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
      val incident = Util.incident()
      val report = Util.report(Some(ReportForm(incident_id = incident.id, body = "test")))
      val other = Util.report()

      ReportsDao.findAll(incidentId = Some(incident.id)).map(_.id).sorted should be(Seq(report.id))
      ReportsDao.findAll(incidentId = Some(0)).map(_.id).sorted should be(Seq.empty)
    }
  }

}
