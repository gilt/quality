package actors

import com.gilt.quality.models.Task
import db.Util
import java.util.UUID
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class DatabaseSpec extends FunSpec with ShouldMatchers {

  it("nextTask") {
    val org = Util.createOrganization()
    val incident = Util.createIncident(org)
    Database.nextTask(incident) should be(Some(Task.ReviewTeam))
  }

}
