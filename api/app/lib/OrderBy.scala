package lib

case class OrderBy(
  column: String,
  direction: OrderBy.Direction
) {

  val sql = direction match {
    case OrderBy.Direction.Ascending => column
    case OrderBy.Direction.Descending => s"$column desc"
  }

}

/**
  * Wrapper to capture a column by which we order data.
  */
object OrderBy {

  sealed trait Direction

  object Direction {

    case object Ascending extends Direction
    case object Descending extends Direction

  }

  def ascending(column: String) = OrderBy(column, Direction.Ascending)

  def descending(column: String) = OrderBy(column, Direction.Descending)

  private val Pattern = "^[a-zA-Z0-9_\\.: ]*$"

  /**
    * value will be column_name:direction
    * direction: asc or desc, defaults to asc
    */
  def apply(value: String): Option[OrderBy] = {
    assert(value.matches(Pattern), "value cannot contain any special characters")

    val parts = value.split(":").map(_.trim).toList
    if (parts.headOption == Some("")) {
      // empty string
      None

    } else {
      parts match {
        case Nil => None
        case column :: Nil => Some(OrderBy(column, Direction.Ascending))
        case column :: "" :: Nil => Some(OrderBy(column, Direction.Ascending))
        case column :: "asc" :: Nil => Some(OrderBy(column, Direction.Ascending))
        case column :: "desc" :: Nil => Some(OrderBy(column, Direction.Descending))
        case other => None
      }
    }
  }

}
