import play.PlayScala
import play.PlayImport.PlayKeys._

name := "quality"

scalaVersion in ThisBuild := "2.11.6"

lazy val core = project
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    // play-json needs this to resolve correctly when not using Gilt's internal mirrors
    resolvers += "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/"
  )

lazy val api = project
  .in(file("api"))
  .dependsOn(core)
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      jdbc,
      anorm,
      "org.postgresql" % "postgresql" % "9.3-1101-jdbc4",
      "com.sendgrid" % "sendgrid-java" % "2.2.0",
      "net.rcarz" % "jira-client" % "0.5"
    ),
    routesImport += "com.gilt.quality.v0.Bindables._"
  )

lazy val www = project
  .in(file("www"))
  .dependsOn(core)
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    routesImport += "com.gilt.quality.v0.Bindables._"
  )

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name <<= name("quality-" + _),
  libraryDependencies ++= Seq(
    ws,
    "org.commonjava.googlecode.markdown4j" % "markdown4j" % "2.2-cj-1.0",
    "org.scalatestplus" %% "play" % "1.1.0" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
  )
)
