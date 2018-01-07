import sbt._
import sbt.Keys._

object SamSerializationSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin
  override def projectSettings = GlobalSettings.commonSettings ++ Seq(
    libraryDependencies += LibraryDependencies.libSecurityBouncyCastle % Provided,
    libraryDependencies += LibraryDependencies.libAwsEncryptionSDK % Provided,
    libraryDependencies += LibraryDependencies.libAwsJavaSdk % Provided,
  )
}