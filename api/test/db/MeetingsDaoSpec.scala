package db

import com.gilt.quality.models.{ Meeting, Severity }
import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class MeetingsDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val meeting = Util.createMeeting()
      val fetched = MeetingsDao.findById(meeting.id).get
      fetched.id should be(meeting.id)
      fetched.agendaItems should be(Seq.empty)
    }
  }

}
