name := "serverless-lambda"

organization := "com.github.dnvriend"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.4"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.217"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "2.0.1"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.1.0"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.223"
libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.8.0"
libraryDependencies += "io.circe" %% "circe-yaml" % "0.6.1"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.16"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7"