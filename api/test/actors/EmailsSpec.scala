package actors

import com.gilt.quality.v0.models._
import db._
import java.util.UUID
import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class EmailsSpec extends FunSpec with ShouldMatchers {

  new play.core.StaticApplication(new java.io.File("."))

  def subscriptions(
    org: Organization,
    publication: Publication,
    team: Option[Team] = None
  ): Seq[Subscription] = {
    val subs = scala.collection.mutable.ListBuffer[Subscription]()
    Emails.eachSubscription(org, publication, team, { s => subs.append(s) })
    subs
  }

  it("eachSubscriber for different publications") {
    val org = Util.createOrganization()
    val user1 = Util.createUser()
    val user2 = Util.createUser()

    subscriptions(org, Publication.IncidentsCreate).map(_.user.guid) should be(Seq.empty)

    Util.createSubscription(org, user1, Publication.IncidentsCreate)
    subscriptions(org, Publication.IncidentsCreate).map(_.user.guid) should be(Seq(user1.guid))
    subscriptions(org, Publication.IncidentsUpdate).map(_.user.guid) should be(Seq.empty)

    Util.createSubscription(org, user2, Publication.IncidentsUpdate)
    subscriptions(org, Publication.IncidentsCreate).map(_.user.guid) should be(Seq(user1.guid))
    subscriptions(org, Publication.IncidentsUpdate).map(_.user.guid) should be(Seq(user2.guid))

    Util.createSubscription(org, user2, Publication.IncidentsCreate)
    subscriptions(org, Publication.IncidentsCreate).map(_.user.guid).sorted should be(Seq(user1.guid, user2.guid).sorted)
    subscriptions(org, Publication.IncidentsUpdate).map(_.user.guid) should be(Seq(user2.guid))
  }

  it("eachSubscriber w/ team filter") {
    val org = Util.createOrganization()
    val user1 = Util.createUser()
    val user2 = Util.createUser()

    Util.createSubscription(org, user1, Publication.IncidentsTeamUpdate)
    Util.createSubscription(org, user2, Publication.IncidentsTeamUpdate)

    val team = Util.createTeam(org)

    subscriptions(org, Publication.IncidentsTeamUpdate, Some(team)).map(_.user.guid) should be(Seq.empty)

    TeamMembersDao.upsert(UsersDao.Default, TeamMemberForm(org, team.key, user1.guid))
    subscriptions(org, Publication.IncidentsTeamUpdate, Some(team)).map(_.user.guid) should be(Seq(user1.guid))

    TeamMembersDao.upsert(UsersDao.Default, TeamMemberForm(org, team.key, user2.guid))
    subscriptions(org, Publication.IncidentsTeamUpdate, Some(team)).map(_.user.guid).sorted should be(Seq(user1.guid, user2.guid).sorted)
  }

}
