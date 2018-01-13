import sbt._

object LibraryDependencies {
  // versions
  val awsSdkVersion = "1.11.263"

  // libraries
  val libAwsJavaSdk: ModuleID = "com.amazonaws" % "aws-java-sdk" % awsSdkVersion
  val libAwsDynamoDBSdk: ModuleID = "com.amazonaws" % "aws-java-sdk-dynamodb" % awsSdkVersion
  val libAwsSnsSdk: ModuleID = "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion
  val libAwsS3Sdk: ModuleID = "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
  val libKinesisSdk: ModuleID = "com.amazonaws" % "aws-java-sdk-kinesis" % awsSdkVersion
  val libGuava: ModuleID = "com.google.guava" % "guava" % "23.0"
  val libAwsLambdaJavaCore: ModuleID = "com.amazonaws" % "aws-lambda-java-core" % "1.2.0"
  val libAwsLambdaJavaEvents: ModuleID = "com.amazonaws" % "aws-lambda-java-events" % "2.0.2"
  val libAwsEncryptionSDK: ModuleID = "com.amazonaws" % "aws-encryption-sdk-java" % "1.3.1"
  val libSecurityBouncyCastle: ModuleID = "org.bouncycastle" % "bcprov-ext-jdk15on" % "1.59"
  val libTypesafeConfig: ModuleID = "com.typesafe" % "config" % "1.3.2"
  val libPureConfig: ModuleID = "com.github.pureconfig" %% "pureconfig" % "0.8.0"
  val libScalaz: ModuleID = "org.scalaz" %% "scalaz-core" % "7.2.18"
  val libAvro4s: ModuleID = "com.github.dnvriend" %% "avro4s-core" % "1.8.3"
  val libAvro: ModuleID = "org.apache.avro" % "avro" % "1.8.2"
  val libAvroCompiler: ModuleID = "org.apache.avro" % "avro-compiler" % "1.8.2"
  val libPlayJson: ModuleID = "com.typesafe.play" %% "play-json" % "2.6.8"
  val libShapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2"
  val libSbtIO: ModuleID = "org.scala-sbt" %% "io" % "1.1.1"
  val libScalajHttp: ModuleID = "org.scalaj" %% "scalaj-http" % "2.3.0"
  val libLogback: ModuleID = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val libScalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  val libAkkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor" % "2.5.9"
  val libAkkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % "2.5.9"

  // testing libs //
  val libScalaCheckTest: ModuleID = "org.scalacheck" %% "scalacheck" % "1.13.5"
  val libScalazScalaTest: ModuleID = "org.typelevel" %% "scalaz-scalatest" % "1.1.2"
  val libScalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.4"

  // sbt plugins //
  val libSbtAssembly: ModuleID = "com.eed3si9n" % "sbt-assembly" % "0.14.6"
}