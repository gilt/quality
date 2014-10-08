import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import scala.concurrent.Future

object Global extends WithFilters(LoggingFilter) {

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound)
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(Results.BadRequest(Json.toJson("Bad request: " + error)))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Logger.error(ex.toString, ex)
    Future.successful(InternalServerError(ex.toString))
  }

}
