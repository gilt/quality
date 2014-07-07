package db

import quality.models.Incident
import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class PlansDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val plan = Util.createPlan()
      val fetched = PlansDao.findById(plan.id).get
      fetched.id should be(plan.id)
      fetched.body should be(plan.body)
    }
  }

  it("find by incident id") {
    running(FakeApplication()) {
      val incident = Util.createIncident()
      val plan = Util.createPlan(Some(PlanForm(incident_id = incident.id, body = "test")))
      val other = Util.createPlan()

      PlansDao.findAll(incidentId = Some(incident.id)).map(_.id).sorted should be(Seq(plan.id))
      PlansDao.findAll(incidentId = Some(0)).map(_.id).sorted should be(Seq.empty)
    }
  }

  it("find by team key") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString
      val incident = Util.createIncident(Some(IncidentForm(team_key = Some(teamKey), severity = Incident.Severity.High.toString, summary = "test")))
      val plan = Util.createPlan(Some(PlanForm(incident_id = incident.id, body = "test")))
      val other = Util.createPlan()

      PlansDao.findAll(teamKey = Some(teamKey)).map(_.id).distinct should be(Seq(plan.id))
      val results = PlansDao.findAll(teamKey = Some("wrong-name")).map(_.id)
      results.contains(other.id) should be(false)
    }
  }

}
