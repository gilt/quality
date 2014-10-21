package controllers

import db.{SubscriptionsDao, UsersDao}
import lib.Validation
import com.gilt.quality.models.{Publication, SubscriptionForm, User}
import com.gilt.quality.models.json._
import play.api.mvc._
import play.api.libs.json._
import java.util.UUID

object Subscriptions extends Controller with Subscriptions

trait Subscriptions {
  this: Controller =>

  def get(
    id: Option[Long],
    organizationKey: Option[String],
    userGuid: Option[java.util.UUID],
    publication: Option[Publication],
    limit: Int = 25,
    offset: Int = 0
  ) = Action { request =>
    val subscriptions = SubscriptionsDao.findAll(
      id = id,
      organizationKey = organizationKey,
      userGuid = userGuid,
      publication = publication,
      limit = limit,
      offset = offset
    )
    Ok(Json.toJson(subscriptions))
  }

  def getById(id: Long) = Action {
    SubscriptionsDao.findById(id) match {
      case None => NotFound
      case Some(subscription) => Ok(Json.toJson(subscription))
    }
  }

  def post() = Action(parse.json) { request =>
    request.body.validate[SubscriptionForm] match {
      case e: JsError => {
        BadRequest(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[SubscriptionForm] => {
        val form = s.get
        SubscriptionsDao.validate(form) match {
          case Nil => {
            val subscription = SubscriptionsDao.create(UsersDao.Default, form)
            Created(Json.toJson(subscription))
          }
          case errors => {
            Conflict(Json.toJson(errors))
          }
        }
      }
    }
  }

  def deleteById(id: Long) = Action { request =>
    SubscriptionsDao.findById(id).map { subscription =>
      SubscriptionsDao.softDelete(UsersDao.Default, subscription)
    }
    NoContent
  }


}
