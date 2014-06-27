package util

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._

class MarkdownSpec extends FunSpec with Matchers {

  it("converts to html") {
    Markdown.toHtml("a") should be("a")
    Markdown.toHtml("this is **bold** text") should be("this is <b>bold</b> text")
  }

}
