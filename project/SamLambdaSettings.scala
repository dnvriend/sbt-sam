import sbt._
import sbt.Keys._
import LibraryDependencies._

object SamLambdaSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
    libraryDependencies += LibraryDependencies.libAwsDynamoDBSdk,
    libraryDependencies += LibraryDependencies.libSecurityBouncyCastle % Provided,
    libraryDependencies += LibraryDependencies.libAwsEncryptionSDK % Provided,
    libraryDependencies += LibraryDependencies.libAwsJavaSdk % Test
  ) ++ GlobalSettings.commonSettings
}