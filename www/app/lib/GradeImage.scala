package lib

import core.Defaults
import com.gilt.quality.v0.models.Team

object GradeImage {

  val Good = Defaults.GoodGrade
  val Bad = Defaults.BadGrade

  def imageTag(
    team: Option[Team],
    score: Option[Int],
    size: Int = 25
  ): String = {
    val icons = team.map(_.icons).getOrElse(Defaults.Icons)
    score match {
      case None => "-"
      case Some(s) => {
        val url = if (Defaults.isGoodGrade(s)) { icons.smileyUrl } else { icons.frownyUrl }
        s"""<img src="$url" height="$size" width="$size" />"""
      }
    }
  }

}
