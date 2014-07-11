package mail

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

    override def deliver(email: Email) {
      println("Mocked delivery of email: " + email)
    }

  }

  it("sends email") {
    val email = emailer.sendHtml(
      to = Person("michael@gilt.com", Some("Mike Bryzek")),
      subject = "test",
      body = "<b>Hello!</b>"
    )

    email.getToAddresses.get(0).toString should be("Mike Bryzek <michael@gilt.com>")
    email.getSubject should be("test")
  }


}
