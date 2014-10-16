package client

import play.api.Play.current

object Api {

  lazy val instance = new com.gilt.quality.Client(
    current.configuration.getString("quality.apiHostname").getOrElse {
      sys.error(s"configuration parameter[quality.apiHostname] is required")
    }
  )

}
