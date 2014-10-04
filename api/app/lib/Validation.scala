package lib

import play.api.libs.json.Json
import play.api.libs.json.JsError

case class ValidationError(code: String, message: String)

object ValidationError {

  implicit val validationErrorWrites = Json.writes[ValidationError]

}

object Validation {

  private val InvalidJsonCode = "invalid_json"
  private val ErrorCode = "validation_error"
  private val ServerError = "server_error"

  def invalidJson(error: JsError): Seq[ValidationError] = {
    invalidJson(error.toString)
  }

  def invalidJson(error: String): Seq[ValidationError] = {
    Seq(ValidationError(InvalidJsonCode, error))
  }

  def error(message: String): Seq[ValidationError] = {
    errors(Seq(message))
  }

  def errors(messages: Seq[String]): Seq[ValidationError] = {
    messages.map { msg => ValidationError(ErrorCode, msg) }
  }

  def serverError(error: String = "Internal Server Error"): Seq[ValidationError] = {
    Seq(ValidationError(ServerError, error))
  }

}
