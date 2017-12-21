import sbt.Keys._
import sbt._
import LibraryDependencies._

object SamOpsSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings =
    GlobalSettings.commonSettings ++ Seq(
    libraryDependencies += libScalajHttp,
    libraryDependencies += libSbtIO,
  )
}