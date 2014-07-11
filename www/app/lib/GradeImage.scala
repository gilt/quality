package lib

case class GradeImage(score: Int) {

  def imageTag(size: Int = 25): String = {
    val filename = if (score <= 50) { "frowny.png" } else { "smiley.png" }
    s"""<img src="/assets/images/$filename" height="$size" width="$size" />"""
  }

}

object GradeImage {

  val Bad = 0
  val Good = 100

  def imageTag(score: Option[Int]): String = {
    score match {
      case None => "-"
      case Some(s) => GradeImage(s).imageTag()
    }
  }

}
