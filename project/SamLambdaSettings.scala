import sbt._
import sbt.Keys._

object SamLambdaSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    crossScalaVersions := Seq("2.11.12", "2.12.5"),
    libraryDependencies += LibraryDependencies.libAwsSnsSdk,
    libraryDependencies += LibraryDependencies.libKinesisSdk,
    libraryDependencies += LibraryDependencies.libAwsDynamoDBSdk,
    libraryDependencies += LibraryDependencies.libAwsSecretsManagerSdk,
    libraryDependencies += LibraryDependencies.libAwsEcsSdk,
    libraryDependencies += LibraryDependencies.libAwsRequestSigner,
    libraryDependencies += LibraryDependencies.libSecurityBouncyCastle % Provided,
    libraryDependencies += LibraryDependencies.libAwsEncryptionSdk % Provided,
    libraryDependencies += LibraryDependencies.libAwsJavaSdk % Test
  ) ++ GlobalSettings.commonSettings
}