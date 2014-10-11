package controllers

import client.Api
import lib.{Pagination, PaginatedCollection}

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def index(page: Int = 0) = Action.async { implicit request =>
    for {
      orgs <- Api.instance.Organizations.get(
        limit = Some(Pagination.DefaultLimit+1),
        offset = Some(page * Pagination.DefaultLimit)
      )
    } yield {
      if (page == 0 && orgs.size == 1) {
        Redirect(routes.Dashboard.index(orgs.head.key))
      } else {
        Ok(views.html.index(PaginatedCollection(page, orgs)))
      }
    }
  }

}
