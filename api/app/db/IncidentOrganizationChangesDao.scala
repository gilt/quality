package db

import com.gilt.quality.models.{Error, IncidentOrganizationChange, User}
import lib.Validation

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

  def process(createdBy: User, form: IncidentOrganizationChange) {
    val errors = validate(form)
    assert(errors.isEmpty, errors.map(_.message).mkString("\n"))

    IncidentsDao.updateOrganization(form.incidentId, form.organizationKey)
  }

}
