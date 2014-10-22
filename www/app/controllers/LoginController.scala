package controllers

import client.Api
import com.gilt.quality.error.ErrorsResponse
import com.gilt.quality.models.{AuthenticationForm, UserForm}
import lib.MainTemplate
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object LoginController extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def redirect = Action {
    Redirect(routes.LoginController.index())
  }

  def index(returnUrl: Option[String]) = Action { implicit request =>
    Ok(views.html.login.index(MainTemplate(), loginForm))
  }

  def postIndex() = Action.async { implicit request =>
    val form = loginForm.bindFromRequest
    form.fold (

      formWithErrors => Future {
        Ok(views.html.login.index(MainTemplate(), formWithErrors))
      },

      validForm => {
        Api.instance.users.postAuthenticate(AuthenticationForm(email = validForm.email)).map { user =>
          Redirect("/").withSession { "user_guid" -> user.guid.toString }
        }.recover {
          case r: ErrorsResponse => {
            // For now, just auto-register the user if the email is valid
            try {
              val user = Await.result(
                Api.instance.users.post(UserForm(email = validForm.email)),
                1000.millis
              )
              Redirect("/").withSession { "user_guid" -> user.guid.toString }
            } catch {
              case r: ErrorsResponse => {
                Ok(views.html.login.index(MainTemplate(), form, Some(r.errors.map(_.message).mkString(", "))))
              }
            }
          }
        }
      }

    )
  }

  case class LoginData(email: String)
  val loginForm = Form(
    mapping(
      "email" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

}
