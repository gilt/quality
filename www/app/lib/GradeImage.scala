package lib

case class GradeImage(score: Int) {

  def imageTag: String = {
    val filename = if (score <= 50) { "frowny.png" } else { "smiley.png" }
    s"""<img src="/assets/images/$filename" height="42" width="42" />"""
  }

}

object GradeImage {

  val Bad = 0
  val Good = 100

}
