addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4")
addSbtPlugin("org.http4s" % "sbt-http4s-org" % "0.17.5")

resolvers ++= Seq(
  "Scalafmt Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Maven Central" at "https://repo1.maven.org/maven2/",
  "Akka repository" at "https://repo.akka.io/maven"
)
