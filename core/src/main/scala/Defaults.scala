package core

object Defaults {

  val GoodGrade = 100
  val BadGrade = 0

  def isGoodGrade(value: Int): Boolean = {
    value >= 50
  }

  val Icons = com.gilt.quality.models.Icons(
    smileyUrl = "/assets/images/smiley.png",
    frownyUrl = "/assets/images/frowny.png"
  )

}

