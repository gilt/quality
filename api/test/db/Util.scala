package db

import com.gilt.quality.models.{Organization, OrganizationForm}
import java.util.UUID

object Util {

  new play.core.StaticApplication(new java.io.File("."))

  def createOrganization(): Organization = {
    OrganizationsDao.create(User.Default,
      OrganizationForm(
        name = UUID.randomUUID.toString
      )
    )
  }

}
