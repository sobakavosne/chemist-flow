import sbt._

object Dependencies {
  lazy val akkaVersion      = "2.6.20"
  lazy val akkaHttpVersion  = "10.2.10"
  lazy val scalaTestVersion = "3.2.15"
  lazy val scalaLogVersion  = "1.2.11"
  lazy val sprayVersion     = "1.3.6"
  lazy val dockerVersion    = "8.9.0"

  lazy val akkaActor    = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  lazy val akkaStream   = "com.typesafe.akka" %% "akka-stream"      % akkaVersion
  lazy val akkaHttp     = "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion
  lazy val scalaLogging = "ch.qos.logback"     % "logback-classic"  % scalaLogVersion
  lazy val spray        = "io.spray"          %% "spray-json"       % sprayVersion
  lazy val docker       = "com.spotify"        % "docker-client"    % dockerVersion
  lazy val akkaTest     = "com.typesafe.akka" %% "akka-testkit"     % akkaVersion      % Test
  lazy val scalaTest    = "org.scalatest"     %% "scalatest"        % scalaTestVersion % Test
}
