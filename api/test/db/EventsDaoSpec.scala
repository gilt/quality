package db

import quality.models.Event
import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class EventsDaoSpec extends FunSpec with Matchers {

  it("finds latest events") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString

      val i1 = Util.createIncident()
      val i2 = Util.createIncident()

      EventsDao.findAll(limit = 2).map(_.model) should be(Seq(Event.Model.Incident, Event.Model.Incident))

      IncidentsDao.softDelete(Util.user, i1)
      val events = EventsDao.findAll(limit = 2)
      events.map(_.model) should be(Seq(Event.Model.Incident, Event.Model.Incident))
      events.head.action should be(Event.Action.Deleted)

      val plan = Util.createPlan()
      EventsDao.findAll(limit = 10).head.model should be(Event.Model.Plan)

      val grade = Util.upsertGrade(Some(GradeForm(plan_id = plan.id, score = 100)))
      EventsDao.findAll(limit = 10).head.model should be(Event.Model.Rating)
    }
  }

}
