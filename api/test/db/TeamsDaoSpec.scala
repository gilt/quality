package db

import org.scalatest.{FunSpec, Matchers}
import org.junit.Assert._

class TeamsDaoSpec extends FunSpec with Matchers {

  new play.core.StaticApplication(new java.io.File("."))

  it("findAll by userGuid") {
    val org = Util.createOrganization()
    val team = Util.createTeam(org)

    val member = Util.createUser()
    val nonMember = Util.createUser()
    TeamMembersDao.upsert(member, TeamMemberForm(org, team.key, member.guid))

    TeamsDao.findAll(org, userGuid = Some(member.guid)).map(_.key) should be(Seq(team.key))
    TeamsDao.findAll(org, userGuid = Some(nonMember.guid)) should be(Seq.empty)
  }

}
