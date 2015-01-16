package db

import com.gilt.quality.v0.models.Team

import org.scalatest.{FunSpec, Matchers}
import org.junit.Assert._
import java.util.UUID

class PagerSpec extends FunSpec with Matchers {

  new play.core.StaticApplication(new java.io.File("."))

  it("eachPage for no results") {
    val org = Util.createOrganization()
    var allKeys = scala.collection.mutable.Set[String]()

    Pager.eachPage[Team] { offset =>
      TeamsDao.findAll(org, offset = offset)
    } { team =>
      allKeys.add(team.key)
    }

    allKeys.toSeq should be(Seq.empty)
  }

  it("eachPage for multiple results across pages") {
    val org = Util.createOrganization()
    val team1 = Util.createTeam(org)
    val team2 = Util.createTeam(org)
    val team3 = Util.createTeam(org)

    var allKeys = scala.collection.mutable.ListBuffer[String]()
    var allOffsets = scala.collection.mutable.ListBuffer[Int]()

    Pager.eachPage[Team] { offset =>
      allOffsets.append(offset)
      TeamsDao.findAll(org, limit = 1, offset = offset)
    } { team =>
      allKeys.append(team.key)
    }

    allOffsets should be(Seq(0, 1, 2, 3))
    allKeys.sorted should be(Seq(team1.key, team2.key, team3.key).sorted)
  }

  it("eachPage for multiple results on one page") {
    val org = Util.createOrganization()
    val team1 = Util.createTeam(org)
    val team2 = Util.createTeam(org)
    val team3 = Util.createTeam(org)

    var allKeys = scala.collection.mutable.ListBuffer[String]()
    var allOffsets = scala.collection.mutable.ListBuffer[Int]()

    Pager.eachPage[Team] { offset =>
      allOffsets.append(offset)
      TeamsDao.findAll(org, offset = offset)
    } { team =>
      allKeys.append(team.key)
    }

    allOffsets should be(Seq(0, 3))
    allKeys.sorted should be(Seq(team1.key, team2.key, team3.key).sorted)
  }

}
