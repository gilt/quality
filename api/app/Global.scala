import actors.MainActor
import com.gilt.quality.v0.models.json._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future
import lib.Validation

object Global extends WithFilters(LoggingFilter) {

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound)
  }

  override def onStart(app: Application): Unit = {
    Logger.info("quality starting")
    global.Actors.mainActor
  }

  override def onStop(app: Application): Unit = {
    Logger.info("quality shutting down")
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(Json.toJson(Validation.badRequest(error))))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Logger.error(ex.toString, ex)
    Future.successful(InternalServerError(Json.toJson(Validation.serverError(ex.toString))))
  }

}

package global {
  import play.api.libs.concurrent.Akka
  object Actors {
    lazy val mainActor = Akka.system.actorOf(MainActor.props(), "main")
  }
}
