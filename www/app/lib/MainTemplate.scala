package lib

case class MainTemplate(
  title: Option[String] = None,
  headTitle: Option[String] = None,
  user: Option[com.gilt.quality.v0.models.User] = None,
  org: Option[com.gilt.quality.v0.models.Organization] = None
)

