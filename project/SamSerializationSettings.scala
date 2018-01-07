import sbt._
import sbt.Keys._
import LibraryDependencies._

object SamSerializationSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin
  override def projectSettings = GlobalSettings.commonSettings ++ Seq(
    libraryDependencies += libScalajHttp,
    libraryDependencies += libGuava,
    libraryDependencies += LibraryDependencies.libSecurityBouncyCastle % Provided,
    libraryDependencies += LibraryDependencies.libAwsEncryptionSDK % Provided,
    libraryDependencies += LibraryDependencies.libAwsJavaSdk % Provided,
  )
}