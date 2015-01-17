package db

import com.gilt.quality.v0.models.{AgendaItem, Error, IncidentOrganizationChange, User}
import lib.Validation
import play.api.db._
import play.api.Play.current

object IncidentOrganizationChangesDao {

  def validate(
    form: IncidentOrganizationChange
  ): Seq[Error] = {
    val incidentErrors = IncidentsDao.findById(form.incidentId) match {
      case None => Seq(s"Incident ${form.incidentId} not found")
      case Some(_) => Seq.empty
    }

    val organizationErrors = OrganizationsDao.findByKey(form.organizationKey) match {
      case None => Seq(s"Organization ${form.organizationKey} not found")
      case Some(_) => Seq.empty
    }

    Validation.errors(incidentErrors ++ organizationErrors)
  }

  def process(user: User, form: IncidentOrganizationChange) {
    val errors = validate(form)
    assert(errors.isEmpty, errors.map(_.message).mkString("\n"))

    val incident = IncidentsDao.findById(form.incidentId).map { incident =>
      DB.withTransaction { implicit c =>
        Pager.eachPage[AgendaItem] { offset =>
          AgendaItemsDao.findAll(
            org = Some(incident.organization),
            incidentId = Some(incident.id),
            offset = offset
          )
        } { item =>
          AgendaItemsDao.softDelete(c, user, item)
        }

        IncidentsDao.updateOrganization(c, user, form.incidentId, form.organizationKey)
      }
    }
  }

}
