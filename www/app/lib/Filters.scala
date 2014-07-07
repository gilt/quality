package lib

/**
 * Helpers for parsing url params, namely empty strings
 */
object Filters {

  def toOption(value: Option[String]): Option[String] = {
    if (value.isEmpty || value.get.trim.isEmpty) {
      None
    } else {
      Some(value.get.trim)
    }
  }

}

