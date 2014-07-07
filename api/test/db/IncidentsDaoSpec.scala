package db

import quality.models.Incident
import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class IncidentsDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val incident = Util.createIncident()
      val fetched = IncidentsDao.findById(incident.id).get
      fetched.id should be(incident.id)
      fetched.team should be(incident.team)
      fetched.severity should be(incident.severity)
      fetched.description should be(incident.description)
    }
  }

  it("create with tags") {
    running(FakeApplication()) {
      val tags = Seq("a", "b")
      val incident = Util.createIncident(Some(Util.incidentForm.copy(tags = Some(tags))))
      IncidentsDao.findById(incident.id).get.tags should be(tags)
    }
  }

  it("create without a team") {
    running(FakeApplication()) {
      val form = IncidentForm(
        team_key = None,
        severity = Incident.Severity.Low.toString,
        summary = "Something happened",
        description = None
      )

      val incident = Util.createIncident(Some(form))
      IncidentsDao.findById(incident.id).get.team should be(None)
    }
  }

  it("Update preserves id") {
    running(FakeApplication()) {
      val newSummary = "update preserves id"
      val incident = Util.createIncident()
      val updated = IncidentsDao.update(
        Util.user,
        incident,
        Util.incidentForm.copy(summary = newSummary)
      )

      val fetched = IncidentsDao.findById(incident.id).get
      fetched.summary should be(newSummary)
      fetched.id should be(incident.id)
    }
  }

  it("Update also updated tags") {
    running(FakeApplication()) {
      val form = Util.incidentForm
      val incident = Util.createIncident(Some(form.copy(tags = Some(Seq("a")))))

      val updated = IncidentsDao.update(Util.user, incident, form.copy(tags = Some(Seq("a", "b"))))
      updated.tags should be(Seq("a", "b"))

      val updated2 = IncidentsDao.update(Util.user, updated, form.copy(tags = Some(Seq("b"))))
      updated2.tags should be(Seq("b"))

      val updated3 = IncidentsDao.update(Util.user, updated2, form.copy(tags = None))
      updated3.tags should be(Seq.empty)
    }
  }

  it("find by team key") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString

      val i1 = Util.createIncident(Some(Util.incidentForm.copy(team_key = Some(teamKey))))
      val i2 = Util.createIncident(Some(Util.incidentForm.copy(team_key = Some(teamKey))))
      val other = Util.createIncident()

      IncidentsDao.findAll(teamKey = Some(teamKey)).map(_.id).sorted should be(Seq(i1.id, i2.id))
      IncidentsDao.findAll(teamKey = Some(UUID.randomUUID.toString)).map(_.id) should be(Seq.empty)
    }
  }

  it("findAll includes plan if available") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString
      val incident = Util.createIncident(Some(Util.incidentForm.copy(team_key = Some(teamKey))))
      val plan = Util.createPlan(Some(PlanForm(incident_id = incident.id, body = "Test", grade = Some(100))))

      val fetched = IncidentsDao.findAll(teamKey = Some(teamKey)).head
      fetched.id should be(incident.id)
      fetched.plan should be(Some(plan))
    }
  }

}
