package core

import org.scalatest.{ FunSpec, Matchers }
import play.api.test._
import play.api.test.Helpers._

class MarkdownSpec extends FunSpec with Matchers {

  it("converts to html") {
    Markdown.toHtml("a").trim should be("<p>a</p>")
    Markdown.toHtml("this is **bold** text").trim should be("<p>this is <strong>bold</strong> text</p>")
  }

}
