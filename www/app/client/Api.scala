package client

import play.api.Play.current

object Api {

  private def configValue(name: String): String = {
    current.configuration.getString(name).getOrElse {
      sys.error(s"configuration parameter[$name] is required")
    }
  }

  lazy val instance = new com.gilt.quality.Client(configValue("quality.url"))

}
