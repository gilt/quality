package lib

import com.gilt.quality.v0.models.Error
import play.api.libs.json.Json
import play.api.libs.json.JsError

object Validation {

  private val InvalidJsonCode = "invalid_json"
  private val ErrorCode = "validation_error"
  private val ServerError = "server_error"
  private val BadRequest = "bad_request"
  private val UserAuthorizationFailedCode = "user_authorization_failed"

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

  def userAuthorizationFailed(): Seq[Error] = {
    Seq(Error(UserAuthorizationFailedCode, "Email address not valid"))
  }

}
