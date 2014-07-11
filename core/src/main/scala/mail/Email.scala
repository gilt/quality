package core.mail

import org.apache.commons.mail.{ DefaultAuthenticator, Email, HtmlEmail }

case class Person(email: String, name: Option[String] = None)

case class Configuration(
  defaultFrom: Person,
  host: String,
  port: Int = 465,
  sslOnConnect: Boolean = true,
  username: Option[String] = None,
  password: Option[String] = None
)


case class Emailer(config: Configuration) {

  def sendHtml(
    from: Option[Person] = None,
    to: Person,
    subject: String,
    body: String
  ): String = {
    val email = new HtmlEmail()
    email.setHostName(config.host)
    email.setSmtpPort(config.port)
    email.setSSLOnConnect(config.sslOnConnect)
    config.username.foreach { username =>
      email.setAuthenticator(new DefaultAuthenticator(username, config.password.getOrElse("")))
    }

    val sender = from.getOrElse(config.defaultFrom)

    email.addTo(to.email, to.name.getOrElse(""))
    email.setFrom(sender.email, sender.name.getOrElse(""))
    email.setSubject(subject)
    email.setHtmlMsg(s"<html>$body</html>")

    deliver(email)
  }

  /**
    * @returns the message id of the underlying MimeMessage
    */
  def deliver(email: Email): String = {
    email.send()
  }

}
