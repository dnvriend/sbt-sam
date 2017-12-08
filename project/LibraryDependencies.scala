import sbt._

object LibraryDependencies {
  // versions //
  val libAwsJavaSdkVersion: String = "1.11.245"
  val libAwsLambdaJavaEventsVersion: String = "2.0.2"
  val libAwsLambdaJavaCoreVersion: String = "1.2.0"
  val libTypesafeConfigVersion: String = "1.3.2"
  val libPureConfigVersion: String = "0.8.0"
  val libScalazVersion: String = "7.2.17"
  val libAvro4sVersion: String = "1.8.0"
  val libCirceYamlVersion: String = "0.6.1"
  val libPlayJsonVersion: String = "2.6.7"
  val libShapelessVersion: String = "2.3.2"
  val libSbtIOVersion: String = "1.1.1"
  val libScalajHttpVersion: String = "2.3.0"
  val libScalazScalaTestVersion: String = "1.1.2"
  val libScalaTestVersion: String = "3.0.4"

  // libraries //
  val libAwsJavaSdk: ModuleID = "com.amazonaws" % "aws-java-sdk" % libAwsJavaSdkVersion
  val libAwsLambdaJavaCore: ModuleID = "com.amazonaws" % "aws-lambda-java-core" % libAwsLambdaJavaCoreVersion
  val libAwsLambdaJavaEvents: ModuleID = "com.amazonaws" % "aws-lambda-java-events" % libAwsLambdaJavaEventsVersion
  val libTypesafeConfig: ModuleID = "com.typesafe" % "config" % libTypesafeConfigVersion
  val libPureConfig: ModuleID = "com.github.pureconfig" %% "pureconfig" % libPureConfigVersion
  val libScalaz: ModuleID = "org.scalaz" %% "scalaz-core" % libScalazVersion
  val libAvro4s: ModuleID = "com.sksamuel.avro4s" %% "avro4s-core" % libAvro4sVersion
  val libCirceYaml: ModuleID = "io.circe" %% "circe-yaml" % libCirceYamlVersion
  val libPlayJson: ModuleID = "com.typesafe.play" %% "play-json" % libPlayJsonVersion
  val libShapeless: ModuleID = "com.chuusai" %% "shapeless" % libShapelessVersion
  val libSbtIO: ModuleID = "org.scala-sbt" %% "io" % libSbtIOVersion
  val libScalajHttp: ModuleID = "org.scalaj" %% "scalaj-http" % libScalajHttpVersion

  // testing libs //
  val libScalazScalaTest: ModuleID = "org.typelevel" %% "scalaz-scalatest" % libScalazScalaTestVersion
  val libScalaTest: ModuleID = "org.scalatest" %% "scalatest" % libScalaTestVersion

  // sbt plugins //
  val libSbtAssembly: ModuleID = "com.eed3si9n" % "sbt-assembly" % "0.14.6"
}