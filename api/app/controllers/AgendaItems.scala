package controllers

import lib.Validation
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._

object AgendaItems extends Controller with AgendaItems

trait AgendaItems {
  this: Controller =>

  def get(
    meeting_id: String,
    task: Option[com.gilt.quality.models.Task],
    limit: Int = 25,
    offset: Int = 0
  ) = TODO


  def getById(meeting_id: String, id: Long) = TODO

  def deleteById(meeting_id: String, id: Long) = TODO

  def post(meeting_id: String) = TODO

}
