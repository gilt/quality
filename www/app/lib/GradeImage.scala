package lib

import com.gilt.quality.models.Team

object GradeImage {

  val Bad = 0
  val Good = 100

  def imageTag(
    team: Option[Team],
    score: Option[Int],
    size: Int = 25
  ): String = {
    score match {
      case None => "-"
      case Some(s) => {
        val filename = if (s <= 50) { "frowny.png" } else { "smiley.png" }
        s"""<img src="/assets/images/$filename" height="$size" width="$size" />"""
      }
    }
  }

}
