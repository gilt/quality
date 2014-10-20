package actors

import com.gilt.quality.models.{PlanForm, Task}
import db.{AgendaItemsDao, FullIncidentForm, IncidentsDao, MeetingsDao, User, Util}
import lib.Email
import org.joda.time.DateTime
import java.nio.file.{Files, Path}
import java.util.UUID
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}
import scala.collection.JavaConversions._

class EmailSpec extends FunSpec with ShouldMatchers {

  new play.core.StaticApplication(new java.io.File("."))

  val targetDir = Email.localDeliveryDir.getOrElse {
    sys.error("Local email delivery not enabled")
  }

  def deleteFiles(dir: Path) {
    Files.newDirectoryStream(targetDir).foreach { f =>
      f.toFile.delete()
    }
  }

  it("syncMeetings") {
    deleteFiles(targetDir)

    // Actors look for meetings that ended in past 12 hours
    val now = new DateTime()
    val org = Util.createOrganization()
    val meetingLastHour = MeetingsDao.upsert(org, now.plusHours(-1))

    val incident = Util.createIncident(org)
    val item = MeetingsDao.upsertAgendaItem(meetingLastHour, incident, Task.ReviewTeam)

    AgendaItemEvents.processCreated(item.id)

    val file = Files.newDirectoryStream(targetDir).head
    val contents = io.Source.fromFile(file.toFile).mkString

    val subject = s"Subject: [PerfectDay] Incident ${incident.id} Added"
    (contents.indexOf(subject) > 0) should be(true)
  }

}
