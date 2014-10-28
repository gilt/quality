package controllers

import lib.MainTemplate
import play.api._
import play.api.mvc._

object LogoutController extends Controller {

  def index() = Action {
    Redirect(routes.LogoutController.confirmation).withNewSession
  }

  def confirmation() = Action { implicit request =>
    Ok(views.html.logout.confirmation(MainTemplate()))
  }

}
