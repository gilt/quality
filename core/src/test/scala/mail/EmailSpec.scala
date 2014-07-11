package core.mail

import org.scalatest.{ FunSpec, Matchers }
import org.apache.commons.mail.Email

class MailSpec extends FunSpec with Matchers {

  val config = Configuration(
    defaultFrom = Person(email = "michael@gilt.com", name = Some("Michael Bryzek")),
    host = "email-smtp.us-east-1.amazonaws.com",
    username = Some("username"),
    password = Some("password")
  )

  val emailer = new Emailer(config) {

    override def deliver(email: Email): String = {
      "email-1"
    }

  }

  it("sends email") {
    val messageId = emailer.sendHtml(
      to = Person("michael@gilt.com", Some("Mike Bryzek")),
      subject = "test",
      body = "<b>Hello!</b>"
    )
    messageId should be("email-1")
  }


}
