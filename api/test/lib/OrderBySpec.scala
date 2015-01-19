package lib

import play.api.test.Helpers._
import org.scalatest.{FunSpec, ShouldMatchers}

class OrderBySpec extends FunSpec with ShouldMatchers {

  it("ascending") {
    OrderBy("meetings.scheduled_at").get.sql should be("meetings.scheduled_at")
    OrderBy("meetings.scheduled_at", OrderBy.Direction.Ascending).sql should be("meetings.scheduled_at")
  }

  it("descending") {
    OrderBy("meetings.scheduled_at", OrderBy.Direction.Descending).sql should be("meetings.scheduled_at desc")
  }

  it("apply for valid strings") {
    OrderBy("meetings.scheduled_at").get.sql should be("meetings.scheduled_at")
    OrderBy("meetings.scheduled_at:").get.sql should be("meetings.scheduled_at")
    OrderBy("meetings.scheduled_at:asc").get.sql should be("meetings.scheduled_at")
    OrderBy("meetings.scheduled_at:desc").get.sql should be("meetings.scheduled_at desc")
  }

  it("apply for invalid strings") {
    OrderBy("meetings.scheduled_at:foo") should be(None)
    OrderBy("") should be(None)
    OrderBy("   ") should be(None)
    OrderBy("   :") should be(None)
    OrderBy("   :  ") should be(None)
    OrderBy(":") should be(None)
    OrderBy(":asc") should be(None)
    OrderBy(":desc") should be(None)
  }

  it("apply asserts invalid characters") {
    an [AssertionError] should be thrownBy {
      OrderBy(";")
    }
  }

}
