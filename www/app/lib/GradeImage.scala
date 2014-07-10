package lib

case class GradeImage(score: Option[Int]) {

  def imageTag(size: Int = 25): String = {
  	score match {
      case None => s" - "
      case Some(v: Int) => {
      	val filename = if (v <= 50) { "frowny.png" } else { "smiley.png" }
    	s"""<img src="/assets/images/$filename" height="$size" width="$size" />"""
      }
    }
  }

}

object GradeImage {

  val Bad = 0
  val Good = 100

}
