package lib

import play.api.Play.current
import java.util.UUID
import java.nio.file.{Path, Paths, Files}
import java.nio.charset.StandardCharsets
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import com.sendgrid._

case class Person(email: String, name: Option[String] = None)

object Email {

  private def config(name: String): String = {
    val value = current.configuration.getString(name).getOrElse {
      sys.error(s"configuration parameter[$name] is required")
    }
    assert(value.trim != "", "Value for configuration parameter[$name] cannot be blank")
    value
  }

  private val from = Person(
    email = config("mail.defaultFromEmail"),
    name = Some(config("mail.defaultFromName"))
  )

  val localDeliveryDir = current.configuration.getString("mail.localDeliveryDir").map(Paths.get(_))

  // Initialize sendgrid on startup to verify that all of our settings
  // are here. If using localDeliveryDir, set password to a test
  // string.
  private val sendgrid = {
    localDeliveryDir match {
      case None => new SendGrid(config("sendgrid.apiUser"), config("sendgrid.apiKey"))
      case Some(_) => new SendGrid(config("sendgrid.apiUser"), "development")
    }
  }

  def sendHtml(
    to: Person,
    subject: String,
    body: String
  ) {
    val email = new SendGrid.Email()
    email.addTo(to.email)
    to.name.map { n => email.addToName(n) }
    email.setFrom(from.email)
    from.name.map { n => email.setFromName(n) }
    email.setSubject(subject)
    email.setHtml(body)

    localDeliveryDir match {
      case Some(dir) => {
        localDelivery(dir, to, subject, body)
      }

      case None => {
        val response = sendgrid.send(email)
        assert(response.getStatus, "Error sending email: " + response.getMessage())
      }
    }
  }

  private def localDelivery(dir: Path, to: Person, subject: String, body: String): String = {
    val timestamp = UrlKey.generate(ISODateTimeFormat.dateTimeNoMillis.print(new DateTime()))

    Files.createDirectories(dir)
    val target = Paths.get(dir.toString, timestamp + "-" + UUID.randomUUID.toString + ".html")
    val name = to.name match {
      case None => to.email
      case Some(name) => s""""$name" <${to.email}">"""
    }

    val bytes = s"""<p>To: $name<br/>
Subject: $subject
</p>
<hr size="1"/>

$body
""".getBytes(StandardCharsets.UTF_8)
    Files.write(target, bytes)

    println(s"email delivered locally to $target")
    s"local-delivery-to-$target"
  }

}
