name := "sbt-sam"

organization := "com.github.dnvriend"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.4"

sbtPlugin := true

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.217"
libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.8.0"
libraryDependencies += "io.circe" %% "circe-yaml" % "0.6.1"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.16"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7"
libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0"

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

