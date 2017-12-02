import sbt._
import Keys._

trait LibraryDependenciesKeys {
  lazy val libAwsJavaSdkVersion = settingKey[String]("")
  lazy val libAwsJavaSdk = settingKey[ModuleID]("AWS SDK for Java")
  lazy val libAwsJavaSdkDynamoDb = settingKey[ModuleID]("")
  lazy val libAwsLambdaJavaCoreVersion = settingKey[String]("")
  lazy val libAwsLambdaJavaCore = settingKey[ModuleID]("")
  lazy val libAwsLambdaJavaEventsVersion = settingKey[String]("")
  lazy val libAwsLambdaJavaEvents = settingKey[ModuleID]("")
  lazy val libScalazScalatestVersion = settingKey[String]("")
  lazy val libScalazScalatest = settingKey[ModuleID]("")
  lazy val libScalaTestVersion = settingKey[String]("")
  lazy val libScalaTest = settingKey[ModuleID]("")
  lazy val libTypesafeConfigVersion = settingKey[String]("")
  lazy val libTypesafeConfig = settingKey[ModuleID]("")
  lazy val libPureConfigVersion = settingKey[String]("")
  lazy val libPureConfig = settingKey[ModuleID]("")
  lazy val libScalazVersion = settingKey[String]("")
  lazy val libScalaz = settingKey[ModuleID]("An extension to the core Scala library for functional programming")
  lazy val libAvro4sVersion = settingKey[String]("")
  lazy val libAvro4s = settingKey[ModuleID]("")
  lazy val libSimulacrumVersion = settingKey[String]("")
  lazy val libSimulacrum = settingKey[ModuleID]("")
  lazy val libCirceYamlVersion = settingKey[String]("")
  lazy val libCirceYaml = settingKey[ModuleID]("")
  lazy val libPlayJsonVersion = settingKey[String]("")
  lazy val libPlayJson = settingKey[ModuleID]("")

  lazy val libScalaMacros: ModuleID = "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  lazy val libSbtAssembly: ModuleID = "com.eed3si9n" % "sbt-assembly" % "0.14.6"
}
object LibraryDependenciesKeys extends LibraryDependenciesKeys

object LibraryDependencies extends AutoPlugin with LibraryDependenciesKeys {
  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin
  
  override def projectSettings = libVersions ++ libs

  lazy val libVersions = Seq(
    libAwsJavaSdkVersion := "1.11.241",
    libAwsLambdaJavaEventsVersion := "2.0.2",
    libAwsLambdaJavaCoreVersion := "1.2.0",
    libScalazScalatestVersion := "1.1.2",
    libScalaTestVersion := "3.0.4",
    libTypesafeConfigVersion := "1.3.2",
    libPureConfigVersion := "0.8.0",
    libScalazVersion := "7.2.17",
    libAvro4sVersion := "1.8.0",
    libSimulacrumVersion := "0.11.0",
    libCirceYamlVersion := "0.6.1",
    libPlayJsonVersion := "2.6.7",
  )

  lazy val libs = Seq(
    libAwsJavaSdk := "com.amazonaws" % "aws-java-sdk" % libAwsJavaSdkVersion.value,
    libAwsJavaSdkDynamoDb := "com.amazonaws" % "aws-java-sdk-dynamodb" % libAwsJavaSdkVersion.value,
    libAwsLambdaJavaCore := "com.amazonaws" % "aws-lambda-java-core" % libAwsLambdaJavaCoreVersion.value,
    libAwsLambdaJavaEvents := "com.amazonaws" % "aws-lambda-java-events" % libAwsLambdaJavaEventsVersion.value,
    libScalazScalatest := "org.typelevel" %% "scalaz-scalatest" % libScalazScalatestVersion.value % Test,
    libScalaTest := "org.scalatest" %% "scalatest" % libScalaTestVersion.value % Test,
    libTypesafeConfig := "com.typesafe" % "config" % libTypesafeConfigVersion.value,
    libPureConfig := "com.github.pureconfig" %% "pureconfig" % libPureConfigVersion.value,
    libScalaz := "org.scalaz" %% "scalaz-core" % libScalazVersion.value,
    libAvro4s := "com.sksamuel.avro4s" %% "avro4s-core" % libAvro4sVersion.value,
    libSimulacrum := "com.github.mpilquist" %% "simulacrum" % libSimulacrumVersion.value,
    libCirceYaml := "io.circe" %% "circe-yaml" % libCirceYamlVersion.value,
    libPlayJson := "com.typesafe.play" %% "play-json" % libPlayJsonVersion.value,
  )
}
