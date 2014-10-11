package db

import java.util.UUID

object User {

  /**
    * Represents changes created by the background actors
    */
  val Actor = User(guid = UUID.fromString("9472ae70-30c2-012c-8f71-0015177442e6"))

  /**
    * TODO: Remove this and replace with actual user guid once we ask
    * people to login
    */
  val Default = Actor

}

case class User(guid: UUID)
