package db

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class GradesDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val grade = Util.grade()
      val fetched = GradesDao.findById(grade.id).get
      fetched.id should be(grade.id)
      fetched.report_id should be(grade.report_id)
      fetched.grade should be(grade.grade)
    }
  }

}
