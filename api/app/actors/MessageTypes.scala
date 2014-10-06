package actors

object MessageTypes {
  case object WorkRequired
  case class SkuToProcess(sku_id: Long)
}
