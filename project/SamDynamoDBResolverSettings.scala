import LibraryDependencies._
import sbt.Keys._
import sbt._

object SamDynamoDBResolverSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin
  override def projectSettings = GlobalSettings.commonSettings ++ Seq(
    crossScalaVersions := Seq("2.11.12", "2.12.7"),
    libraryDependencies += libScalajHttp,
    libraryDependencies += libGuava,
    libraryDependencies += libAwsDynamoDBSdk,
  )
}