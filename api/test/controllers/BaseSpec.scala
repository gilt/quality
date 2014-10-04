package controllers

import com.gilt.quality.models._
import java.util.UUID
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

abstract class BaseSpec extends PlaySpec with OneServerPerSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit override lazy val port = 8002
  implicit override lazy val app: FakeApplication = FakeApplication()

  val client = new com.gilt.quality.Client(s"http://localhost:$port")

  def createMeeting(
    form: MeetingForm = createMeetingForm()
  ): Meeting = {
    await(client.meetings.post(form))
  }

  def createMeetingForm() = {
    MeetingForm(
      scheduledAt = (new DateTime()).plus(1)
    )
  }

}
