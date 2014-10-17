package lib

import core.mail.{ Configuration, Emailer, Person }
import play.api.Play.current
import java.util.UUID
import java.nio.file.{Path, Paths, Files}
import java.nio.charset.StandardCharsets

object Email {

  private def config(name: String): String = {
    current.configuration.getString(name).getOrElse {
      sys.error(s"configuration parameter[$name] is required")
    }
  }

  private val localDeliveryDir = current.configuration.getString("mail.local_delivery_dir")

  private val emailer = Emailer(Configuration(
    defaultFrom = Person(config("mail.default_from_email"), Some(config("mail.default_from_name"))),
    host = config("mail.host"),
    port = config("mail.port").toInt,
    sslOnConnect = config("mail.sslOnConnect").toBoolean,
    username = current.configuration.getString("mail.username"),
    password = current.configuration.getString("mail.password")
  ))

  def sendHtml(
    to: Person,
    subject: String,
    body: String
  ): String = {
    localDeliveryDir match {
      case None => emailer.sendHtml(to = to, subject = subject, body = body)
      case Some(dir) => localDelivery(Paths.get(dir), to, subject, body)
    }
  }

  private def localDelivery(dir: Path, to: Person, subject: String, body: String): String = {
    Files.createDirectories(dir)
    val target = Paths.get(dir.toString, UUID.randomUUID.toString + ".html")
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
