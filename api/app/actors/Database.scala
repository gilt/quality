package actors

import com.gilt.quality.models.{AgendaItemForm, Meeting, MeetingForm, Organization, Task}
import db.{FullAgendaItemForm, AgendaItemsDao, IncidentsDao, FullMeetingForm, MeetingsDao, OrganizationsDao, User}
import org.joda.time.DateTime

object Database {

  val AllTasks = Seq(Task.ReviewTeam, Task.ReviewPlan)

  def ensureAllOrganizationHaveUpcomingMeetings() {
    OrganizationsDao.findAll().foreach { org =>
      ensureOrganizationHasUpcomingMeetings(org)
    }
  }

  def ensureOrganizationHasUpcomingMeetings(org: Organization) {
    MeetingSchedule.findByOrganization(org).map { schedule =>
      schedule.upcomingDates.foreach { date =>
        MeetingsDao.findAll(
          org = Some(org),
          scheduledAt = Some(date),
          limit = 1
        ).headOption.getOrElse {
          println(s" -- scheduling org[${org.key}] meeting for $date")
          MeetingsDao.create(
            User.Actor,
            FullMeetingForm(
              org,
              MeetingForm(
                scheduledAt = date
              )
            )
          )
        }
      }
    }
  }

  def assignIncident(incidentId: Long) {

    IncidentsDao.findById(incidentId).map { incident =>
      // Only assign incidents for organizations w/ meetings
      // enabled. These orgs will always have upcoming meetings
      // (managed by the EnsureUpcoming message above)
      bestNextMeetingForOrg(incident.organization).map { meeting =>
        val incidentTasks = AgendaItemsDao.findAll(
          incidentId = Some(incidentId)
        ).map(_.task)

        AllTasks.find { t => !incidentTasks.contains(t) } match {
          case None => {
            // All tasks completed or scheduled
          }
          case Some(task) => {
            AgendaItemsDao.findAll(
              meetingId = Some(meeting.id),
              incidentId = Some(incidentId),
              limit = 1
            ).headOption match {
              case None => {
                println("Creating agenda item for incideint " + incidentId)
                AgendaItemsDao.create(
                  User.Actor,
                  FullAgendaItemForm(
                    meeting,
                    AgendaItemForm(
                      incidentId = incidentId,
                      task = task
                    )
                  )
                )
              }
              case Some(_) => {
                // Incident already part of this next meeting
              }
            }
          }
        }
      }
    }
  }

  def bestNextMeetingForOrg(org: Organization): Option[Meeting] = {
    val twelveHoursFromNow = (new DateTime()).plus(3600*12*1000l)
    MeetingsDao.findAll(
      org = Some(org),
      isUpcoming = Some(true)
    ).reverse.find(_.scheduledAt.isAfter(twelveHoursFromNow))
  }

}

