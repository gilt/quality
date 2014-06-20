package db

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._
import java.util.UUID

class IncidentsDaoSpec extends FunSpec with Matchers {

  val user = User(guid = UUID.randomUUID)

  val form = IncidentForm(
    team_key = "architecture",
    severity = "low",
    summary = "Something happened",
    description = None
  )

  it("find by id") {
    running(FakeApplication()) {
      val incident = IncidentsDao.create(user, form)
      val fetched = IncidentsDao.findById(incident.id).get
      fetched.id should be(incident.id)
      fetched.team_key should be(incident.team_key)
      fetched.severity should be(incident.severity)
      fetched.description should be(incident.description)
    }
  }

  it("find by team key") {
    running(FakeApplication()) {
      val teamKey = UUID.randomUUID.toString

      val i1 = IncidentsDao.create(user, form.copy(team_key = teamKey))
      val i2 = IncidentsDao.create(user, form.copy(team_key = teamKey))
      val other = IncidentsDao.create(user, form)

      IncidentsDao.findAll(teamKey = Some(teamKey)).map(_.id).sorted should be(Seq(i1.id, i2.id))
      IncidentsDao.findAll(teamKey = Some(UUID.randomUUID.toString)).map(_.id) should be(Seq.empty)
    }
  }

}
