import LibraryDependencies._
import sbt.Keys._
import sbt._

object SamDynamoDBResolverSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin
  override def projectSettings = GlobalSettings.commonSettings ++ Seq(
    libraryDependencies += libScalajHttp,
    libraryDependencies += libGuava,
    libraryDependencies += libAwsDynamoDBSdk,
  )
}