package controllers

import com.gilt.quality.models.{AgendaItem, AgendaItemForm, Task}
import com.gilt.quality.error.ErrorsResponse
import org.joda.time.DateTime

import play.api.test._
import play.api.test.Helpers._

class AgendaItemsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "POST /meetings/:meeting_id/agenda_items" in new WithServer {
    val meeting = createMeeting()
    val incident = createIncident()
    val item = createAgendaItem(
      meeting = meeting,
      form = AgendaItemForm(
        incidentId = incident.id,
        task = Task.ReviewTeam
      )
    )

    println("ITEM: " + item)
    //val item = createAgendaItem()
  }

}
