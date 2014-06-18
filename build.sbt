name := "quality"

scalaVersion in ThisBuild := "2.11.1"

lazy val api = project
  .in(file("api"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      jdbc,
      anorm,
      ws,
      "org.postgresql" % "postgresql" % "9.3-1101-jdbc4"
    )
  )

lazy val www = project
  .in(file("www"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT"
  )

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name <<= name("quality-" + _),
  libraryDependencies ++= Seq(
    ws,
    "org.scalatest" %% "scalatest" % "2.2.0" % "test"
  ),
  scalacOptions += "-feature"
)
