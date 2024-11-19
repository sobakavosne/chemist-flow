import sbt._

object Dependencies {
  lazy val akkaVersion       = "2.10.0"
  lazy val scalaTestVersion  = "3.2.15"
  lazy val scalaLogVersion   = "1.4.6"
  lazy val dockerVersion     = "8.9.0"
  lazy val catsEffectVersion = "3.3.11"
  lazy val circeVersion      = "0.14.5"
  lazy val pureconfigVersion = "0.17.1"
  lazy val http4sVersion     = "0.23.29"
  lazy val log4catsVersion   = "2.7.0"

  lazy val akkaActor           = "com.typesafe.akka"     %% "akka-actor-typed"         % akkaVersion
  lazy val akkaStream          = "com.typesafe.akka"     %% "akka-stream-typed"        % akkaVersion
  lazy val akkaCluster         = "com.typesafe.akka"     %% "akka-cluster-typed"       % akkaVersion
  lazy val akkaDistributedData = "com.typesafe.akka"     %% "akka-distributed-data"    % akkaVersion
  lazy val scalaLogging        = "ch.qos.logback"         % "logback-classic"          % scalaLogVersion
  lazy val log4cats            = "org.typelevel"         %% "log4cats-core"            % log4catsVersion
  lazy val docker              = "com.spotify"            % "docker-client"            % dockerVersion
  lazy val catsEffect          = "org.typelevel"         %% "cats-effect"              % catsEffectVersion
  lazy val circeCore           = "io.circe"              %% "circe-core"               % circeVersion
  lazy val circeGeneric        = "io.circe"              %% "circe-generic"            % circeVersion
  lazy val circeParser         = "io.circe"              %% "circe-parser"             % circeVersion
  lazy val pureconfig          = "com.github.pureconfig" %% "pureconfig"               % pureconfigVersion
  lazy val http4sEmberClient   = "org.http4s"            %% "http4s-ember-client"      % http4sVersion
  lazy val http4sEmberServer   = "org.http4s"            %% "http4s-ember-server"      % http4sVersion
  lazy val http4sCirce         = "org.http4s"            %% "http4s-circe"             % http4sVersion
  lazy val http4sDSL           = "org.http4s"            %% "http4s-dsl"               % http4sVersion
  lazy val akkaTestTyped       = "com.typesafe.akka"     %% "akka-actor-testkit-typed" % akkaVersion      % Test
  lazy val scalaTest           = "org.scalatest"         %% "scalatest"                % scalaTestVersion % Test
}
