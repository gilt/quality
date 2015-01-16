package controllers

import db.UsersDao
import lib.Validation
import com.gilt.quality.v0.models.{AuthenticationForm, User, UserForm}
import com.gilt.quality.v0.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID

object Users extends Controller with Users

trait Users {
  this: Controller =>

  def get(
    guid: Option[java.util.UUID],
    email: Option[String]
  ) = Action {
    if (guid.isEmpty && email.isEmpty) {
      BadRequest(Json.toJson(Validation.error("email or guid must be specified")))
    } else {
      val users = UsersDao.findOne(
        guid = guid,
        email = email
      ) match {
        case None => Seq.empty
        case Some(u) => Seq(u)
      }
      Ok(Json.toJson(users))
    }
  }

  def getByGuid(guid: UUID) = Action {
    UsersDao.findByGuid(guid) match {
      case None => NotFound
      case Some(user) => Ok(Json.toJson(user))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[UserForm] match {
      case e: JsError => {
        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[UserForm] => {
        val form = s.get
        UsersDao.validate(form) match {
          case Nil => {
            val user = UsersDao.create(UsersDao.Default, form)
            Created(Json.toJson(user))
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def postAuthenticate() = Action(parse.json) { request =>
    request.body.validate[AuthenticationForm] match {
      case e: JsError => {
        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[AuthenticationForm] => {
        val form = s.get
        UsersDao.findByEmail(form.email.trim) match {
          case None => Conflict(Json.toJson(Validation.userAuthorizationFailed()))
          case Some(u) => Ok(Json.toJson(u))
        }
      }
    }
  }

}
