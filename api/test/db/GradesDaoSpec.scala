package db

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class GradesDaoSpec extends FunSpec with Matchers {

  it("find by id") {
    running(FakeApplication()) {
      val grade = Util.upsertGrade()
      val fetched = GradesDao.findById(grade.id).get
      fetched.id should be(grade.id)
      fetched.plan_id should be(grade.plan_id)
      fetched.score should be(grade.score)
    }
  }

  it("find by plan id") {
    running(FakeApplication()) {
      val plan = Util.createPlan()
      val grade = Util.upsertGrade(Some(GradeForm(plan_id = plan.id, score = 100)))
      val other = Util.upsertGrade()

      GradesDao.findAll(planId = Some(plan.id)).map(_.id).sorted should be(Seq(grade.id))
      GradesDao.findAll(planId = Some(0)).map(_.id).sorted should be(Seq.empty)
    }
  }
}
