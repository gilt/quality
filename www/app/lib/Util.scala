package lib

import org.joda.time.{DateTime, Minutes}
import com.gilt.quality.v0.models.Meeting

object Util {

  val GitHubUrl = "https://github.com/gilt/quality"

  /**
    * General idea is to check if a meeting is starting around
    * now. This is used in the UI to highlight this meeting (e.g. by
    * making it bold) so it is easy for the human to see that this is
    * the current meeting.
    */
  def isAroundNow(dateTime: DateTime): Boolean = {
    val minutes = Minutes.minutesBetween(DateTime.now, dateTime).getMinutes
    minutes >= -60 && minutes < 720
  }

  def meetingStatus(meeting: Meeting): String = {
    meeting.adjournedAt match {
      case Some(_) => "Adjourned"
      case None => {
        if (meeting.scheduledAt.isBeforeNow) {
          "In Progress"
        } else {
          "Scheduled"
        }
      }
    }
  }

  def meetingLabel(meeting: Meeting): Option[String] = {
    meeting.adjournedAt match {
      case Some(_) => None
      case None => {
        val minutes = Minutes.minutesBetween(DateTime.now, meeting.scheduledAt).getMinutes
        if (minutes < 0) {
          if (minutes > -5) {
            Some("Starting Soon")
          } else {
            None
          }
        } else if (minutes == 0) {
          Some("Starting Now")
        } else if (minutes < 60) {
          Some("Live")
        } else {
          None
        }
      }
    }
  }

}
