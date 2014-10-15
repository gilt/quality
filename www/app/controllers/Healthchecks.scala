package controllers

import play.api._
import play.api.mvc._

object Healthchecks extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   *  Verifies we can communicate with the client API
   */
  def index() = Action.async { request =>
    for {
      orgs <- client.Api.instance.organizations.get(limit = Some(1))
    } yield {
      Ok("healthy")
    }
  }

}
