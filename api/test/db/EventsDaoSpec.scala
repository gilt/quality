package db

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class EventsDaoSpec extends FunSpec with Matchers {

  it("find latest events") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString

      val i1 = Util.createIncident()
      val i2 = Util.createIncident()

      EventsDao.findAll(limit = 2).map(_.model.toString).sorted should be(Seq("incident", "incident"))
    }
  }

}
