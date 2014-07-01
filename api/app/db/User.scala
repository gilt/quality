package db

import java.util.UUID

object User {

  val Default = User(guid = UUID.randomUUID) // TODO

}

case class User(guid: UUID)
