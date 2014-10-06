package lib

import com.gilt.quality.models.Error
import play.api.libs.json.Json
import play.api.libs.json.JsError

object Validation {

  private val InvalidJsonCode = "invalid_json"
  private val ErrorCode = "validation_error"
  private val ServerError = "server_error"
  private val BadRequest = "bad_request"

  def badRequest(error: String): Seq[Error] = {
    Seq(Error(BadRequest, error))
  }

  def invalidJson(error: JsError): Seq[Error] = {
    invalidJson(error.toString)
  }

  def invalidJson(error: String): Seq[Error] = {
    Seq(Error(InvalidJsonCode, error))
  }

  def error(message: String): Seq[Error] = {
    errors(Seq(message))
  }

  def errors(messages: Seq[String]): Seq[Error] = {
    messages.map { msg => Error(ErrorCode, msg) }
  }

  def serverError(error: String = "Internal Server Error"): Seq[Error] = {
    Seq(Error(ServerError, error))
  }

}
