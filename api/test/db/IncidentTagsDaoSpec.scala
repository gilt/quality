package db

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class IncidentTagsDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val tag = Util.createIncidentTag()
      val fetched = IncidentTagsDao.findById(tag.id).get
      fetched.id should be(tag.id)
      fetched.incident_id should be(tag.incident_id)
      fetched.tag should be(tag.tag)
    }
  }

  it("find by incident id") {
    running(FakeApplication()) {
      val incident = Util.createIncident()
      val tag = Util.createIncidentTag(Some(IncidentTagForm(incident_id = incident.id, tag = "test")))
      val other = Util.createIncidentTag()

      IncidentTagsDao.findAll(incidentId = Some(incident.id)).map(_.id).sorted should be(Seq(tag.id))
      IncidentTagsDao.findAll(incidentId = Some(0)).map(_.id).sorted should be(Seq.empty)
    }
  }
}
