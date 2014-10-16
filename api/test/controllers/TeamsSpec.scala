package controllers

import com.gilt.quality.models.{Icons, Team, TeamForm, UpdateTeamForm}
import com.gilt.quality.error.ErrorsResponse
import db.Defaults
import java.util.UUID

import play.api.test._
import play.api.test.Helpers._

class TeamsSpec extends BaseSpec {

  import scala.concurrent.ExecutionContext.Implicits.global
  lazy val org = createOrganization()

  "POST /:org/teams" in new WithServer {
    val key = UUID.randomUUID.toString
    val team = createTeam(org, TeamForm(key = key))
    team.key must be(key)
  }

  "POST /:org/teams w/ email address" in new WithServer {
    val email = UUID.randomUUID.toString + "@quality.mailinator.com"
    val team = createTeam(org, TeamForm(key = UUID.randomUUID.toString, email = Some(email)))
    team.email must be(Some(email))

    intercept[ErrorsResponse] {
      createTeam(org, TeamForm(key = UUID.randomUUID.toString, email = Some("bad")))
    }.errors.map(_.message) must be (Seq("Email address is not valid"))
  }

  "POST /:org/teams w/ icons" in new WithServer {
    val smileyUrl = "http://localhost/s.jpg"
    val frownyUrl = "http://localhost/f.jpg"

    val team = createTeam(
      org,
      TeamForm(
        key = UUID.randomUUID.toString, 
        smileyUrl = Some(smileyUrl),
        frownyUrl = Some(frownyUrl)
      )
    )

    team.icons.smileyUrl must be(smileyUrl)
    team.icons.frownyUrl must be(frownyUrl)
  }

  "PUT /:org/teams updates icons" in new WithServer {
    val smileyUrl = "http://localhost/s.jpg"
    val frownyUrl = "http://localhost/f.jpg"

    val team = createTeam(org)
    team.icons must be(Defaults.Icons)

    val updated = await(
      client.teams.putByOrgAndKey(
        org = org.key,
        key = team.key,
        updateTeamForm = UpdateTeamForm(
          smileyUrl = Some(smileyUrl),
          frownyUrl = Some(frownyUrl)
        )
      )
    )

    updated.icons.smileyUrl must be(smileyUrl)
    updated.icons.frownyUrl must be(frownyUrl)
  }

  "PUT /:org/teams updates email" in new WithServer {
    val team = createTeam(org)
    team.email must be(None)

    val updated = await(
      client.teams.putByOrgAndKey(
        org = org.key,
        key = team.key,
        updateTeamForm = UpdateTeamForm(
          email = Some("foo@gilt.com")
        )
      )
    )

    updated.email must be(Some("foo@gilt.com"))
  }

  "POST /:org/teams use default icons" in new WithServer {
    createTeam(org).icons must be(Defaults.Icons)
  }

  "POST /:org/teams validates that key cannot be reused" in new WithServer {
    val team = createTeam(org)

    intercept[ErrorsResponse] {
      createTeam(org, TeamForm(key = team.key))
    }.errors.map(_.message) must be (Seq(s"Team with key[${team.key}] already exists"))
  }

  "DELETE /:org/teams/:key" in new WithServer {
    val team = createTeam(org)
    await(client.teams.deleteByOrgAndKey(org.key, team.key)) must be(Some(()))
    await(client.teams.getByOrg(org.key, key = Some(team.key))) must be(Seq.empty)
  }

  "GET /:org/teams" in new WithServer {
    val team1 = createTeam(org)
    val team2 = createTeam(org)

    await(client.teams.getByOrg(org.key, key = Some(UUID.randomUUID.toString))) must be(Seq.empty)
    await(client.teams.getByOrg(org.key, key = Some(team1.key))).head must be(team1)
    await(client.teams.getByOrg(org.key, key = Some(team2.key))).head must be(team2)
  }

  "GET /:org/teams/:key" in new WithServer {
    val team = createTeam(org)
    await(client.teams.getByOrgAndKey(org.key, team.key)) must be(Some(team))
    await(client.teams.getByOrgAndKey(org.key, UUID.randomUUID.toString)) must be(None)
  }

}
