import Dependencies._

ThisBuild / scalaVersion     := "3.3.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.chemist.flow"
ThisBuild / organizationName := "chemist.flow"

Compile / mainClass   := Some("app.Main")
Compile / scalaSource := baseDirectory.value / "src" / "main" / "scala"

enablePlugins(DockerPlugin, JavaAppPackaging)

Docker / packageName        := "chemist-flow"
Docker / dockerExposedPorts := Seq(8081)

Test / scalaSource := baseDirectory.value / "src" / "test" / "scala"

lazy val root = (project in file("."))
  .settings(
    name := "chemist-flow",
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
      akkaCluster.cross(CrossVersion.for3Use2_13),
      akkaDistributedData.cross(CrossVersion.for3Use2_13),
      akkaActor.cross(CrossVersion.for3Use2_13),
      // akkaTest.cross(CrossVersion.for3Use2_13),
      // docker
    )
  )

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
