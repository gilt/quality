package lib

import core.Defaults
import com.gilt.quality.models.Team

object GradeImage {

  val Bad = 0
  val Good = 100

  def imageTag(
    team: Option[Team],
    score: Option[Int],
    size: Int = 25
  ): String = {
    val icons = team.map(_.icons).getOrElse(Defaults.Icons)
    score match {
      case None => "-"
      case Some(s) => {
        val url = if (s <= 50) { icons.frownyUrl } else { icons.smileyUrl }
        s"""<img src="$url" height="$size" width="$size" />"""
      }
    }
  }

}
