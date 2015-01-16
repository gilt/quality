package core

import com.gilt.quality.v0.models.Task

object Defaults {

  val GoodGrade = 100
  val BadGrade = 0

  def isGoodGrade(value: Int): Boolean = {
    value >= 50
  }

  val Icons = com.gilt.quality.v0.models.Icons(
    smileyUrl = "/assets/images/smiley.png",
    frownyUrl = "/assets/images/frowny.png"
  )

 def taskLabel(task: Task): String = {
    task match {
      case Task.ReviewTeam => "review team assignment"
      case Task.ReviewPlan => "review the prevention plan"
      case Task.UNDEFINED(key) => key
    }
  }

}

