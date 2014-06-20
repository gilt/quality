package db

import play.api.libs.json._

case class Severity(key: String)

object Severity {

  implicit val severityWrites = Json.writes[Severity]

  val Low = Severity("low")
  val High = Severity("high")

  private val All = Seq(Low, High)

  def fromString(value: String): Option[Severity] = {
    All.find(_.key == value)
  }

}
