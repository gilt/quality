package db

import java.util.UUID

object User {

  val Default = User(guid = "9472ae70-30c2-012c-8f71-0015177442e6") // TODO

}

case class User(guid: UUID)
