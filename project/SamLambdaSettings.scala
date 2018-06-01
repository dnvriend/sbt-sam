import sbt._
import sbt.Keys._

object SamLambdaSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    crossScalaVersions := Seq("2.11.12", "2.12.5"),
    libraryDependencies += LibraryDependencies.libAwsSnsSdk,
    libraryDependencies += LibraryDependencies.libAwsKinesisSdk,
    libraryDependencies += LibraryDependencies.libAwsDynamoDBSdk,
    libraryDependencies += LibraryDependencies.libAwsSecretsManagerSdk,
    libraryDependencies += LibraryDependencies.libAwsEcsSdk,
    libraryDependencies += LibraryDependencies.libAwsXRayCoreSdk,
    libraryDependencies += LibraryDependencies.libAwsXRaySdkAwsInstrumentorSdk,
    libraryDependencies += LibraryDependencies.libJava8ScalaCompat,
    libraryDependencies += LibraryDependencies.libAwsRequestSigner,
    libraryDependencies += LibraryDependencies.libSecurityBouncyCastle % Provided,
    libraryDependencies += LibraryDependencies.libAwsEncryptionSDK % Provided,
    libraryDependencies += LibraryDependencies.libAwsJavaSdk % Test
  ) ++ GlobalSettings.commonSettings
}