package controllers

import client.Api
import com.gilt.quality.v0.errors.UnitResponse
import com.gilt.quality.v0.models.Team
import lib.{ Pagination, PaginatedCollection }
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import java.util.UUID
import java.text.NumberFormat

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._

object TeamMembers extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def memberSummary(
    org: String,
    key: String
  ) = OrgAction.async { implicit request =>
    for {
      memberSummary <- Api.instance.teams.getMemberSummaryByOrgAndKey(org, key).map { summary => Some(summary) }.recover {
        case UnitResponse(404) => None
      }
    } yield {
      val numberMembers = memberSummary.map(_.numberMembers).getOrElse(0l)
      Ok(Json.toJson(Map("number_members" -> NumberFormat.getIntegerInstance.format(numberMembers))))
    }
  }

  def add(
    org: String,
    key: String
  ) = TeamAction { implicit request =>
    Ok(views.html.team_members.add(request.mainTemplate(), request.team, memberForm))
  }

  def postAdd(
    org: String,
    key: String
  ) = TeamAction.async { implicit request =>
    val boundForm = memberForm.bindFromRequest
    boundForm.fold (

      formWithErrors => Future {
        Ok(views.html.team_members.add(request.mainTemplate(), request.team, formWithErrors))
      },

      memberForm => {
        Await.result(Api.instance.users.get(email = Some(memberForm.email)), 1000.millis).headOption match {
          case None => Future {
            Ok(views.html.team_members.add(request.mainTemplate(), request.team, boundForm, Some("User not found")))
          }
          case Some(user) => {
            Api.instance.teams.putMembersByOrgAndKeyAndUserGuid(
              org = request.org.key,
              key = request.team.key,
              userGuid = user.guid
            ).map { member =>
              Redirect(routes.Teams.show(request.org.key, request.team.key)).flashing("success" -> "Member added")
            }.recover {
              case response: com.gilt.quality.v0.errors.ErrorsResponse => {
                Ok(views.html.team_members.add(request.mainTemplate(), request.team, boundForm, Some(response.errors.map(_.message).mkString(", "))))
              }
            }
          }
        }
      }
    )
  }

  def postRemove(
    org: String,
    key: String,
    userGuid: Option[UUID] = None
  ) = TeamAction.async { request =>
    for {
      member <- Api.instance.teams.deleteMembersByOrgAndKeyAndUserGuid(
        org = request.org.key,
        key = request.team.key,
        userGuid = userGuid.getOrElse(request.user.guid)
      )
    } yield {
      Redirect(routes.Teams.show(request.org.key, request.team.key)).flashing("success" -> "Team member removed")
    }
  }

  def postJoin(
    org: String,
    key: String
  ) = TeamAction.async { request =>
    for {
      member <- Api.instance.teams.putMembersByOrgAndKeyAndUserGuid(
        org = request.org.key,
        key = request.team.key,
        userGuid = request.user.guid
      )
    } yield {
      Redirect(routes.Teams.show(request.org.key, request.team.key)).flashing("success" -> "You are on this team")
    }
  }

  case class MemberForm(
    email: String
  )

  private val memberForm = Form(
    mapping(
      "email" -> text
    )(MemberForm.apply)(MemberForm.unapply)
  )

}
