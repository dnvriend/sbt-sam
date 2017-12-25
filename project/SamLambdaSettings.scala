import sbt._
import sbt.Keys._
import LibraryDependencies._

object SamLambdaSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    libraryDependencies += LibraryDependencies.libDynamoDBSdk % Provided
  ) ++ GlobalSettings.commonSettings
}