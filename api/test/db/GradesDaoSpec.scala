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
      fetched.score should be(grade.score)
    }
  }

  it("find by report id") {
    running(FakeApplication()) {
      val report = Util.report()
      val grade = Util.grade(Some(GradeForm(report_id = report.id, score = 100)))
      val other = Util.grade()

      GradesDao.findAll(reportId = Some(report.id)).map(_.id).sorted should be(Seq(grade.id))
      GradesDao.findAll(reportId = Some(0)).map(_.id).sorted should be(Seq.empty)
    }
  }
}
