package db

import com.gilt.quality.models.{ Meeting, Severity }
import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class AgendaItemsDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val meeting = Util.createMeeting()
      val item = Util.createAgendaItem()
      val fetched = AgendaItemsDao.findById(item.id).get
      fetched.id should be(item.id)
    }
  }

}
