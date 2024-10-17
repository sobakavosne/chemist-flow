import Dependencies._

ThisBuild / scalaVersion     := "2.12.19"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.chemist.flow"
ThisBuild / organizationName := "chemist.flow"

Compile / mainClass   := Some("app.Main")
Compile / scalaSource := baseDirectory.value / "src" / "main" / "scala"

enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)
dockerExposedPorts ++= Seq(8081)
Docker / packageName := "chemist-flow"
// dockerEnvVars ++= Map(("CHEMIST_FLOW_HOST", "localhost"), ("CHEMIST_FLOW_PORT", "8081"))
// dockerExposedVolumes := Seq("/opt/docker/.logs", "/opt/docker/.keys")

Test / scalaSource := baseDirectory.value / "src" / "test" / "scala"

lazy val root = (project in file("."))
  .settings(
    name := ".",
    libraryDependencies ++= Seq(
      scalaLogging,
      akkaStream,
      scalaTest,
      akkaActor,
      akkaHttp,
      akkaTest,
      docker,
      spray
    )
  )

scalacOptions ++= Seq(
  "-deprecation", // Warn about the usage of deprecated features
  "-feature",     // Warn about features that should be enabled explicitly
  "-unchecked", // Enable additional warnings where generated code depends on assumptions
  "-Xlint:unused" // Enable warnings for unused imports
)
