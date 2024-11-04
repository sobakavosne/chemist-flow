import Dependencies._

ThisBuild / scalaVersion     := "3.3.3"
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
      scalaTest,
      circeGeneric,
      circeParser,
      // circeCore,
      catsEffect,
      log4cats,
      http4sEmberClient,
      http4sEmberServer,
      http4sCirce,
      http4sDSL,
      pureconfig.cross(CrossVersion.for3Use2_13),
      akkaStream.cross(CrossVersion.for3Use2_13),
      // akkaCluster.cross(CrossVersion.for3Use2_13)
      // akkaActor.cross(CrossVersion.for3Use2_13),
      // akkaTest.cross(CrossVersion.for3Use2_13),
      // docker
    )
  )
