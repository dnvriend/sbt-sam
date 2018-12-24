import sbt.Keys._
import sbt._
import LibraryDependencies._

object SamAkkaStreamSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    crossScalaVersions := Seq("2.11.12", "2.12.7"),
    libraryDependencies += libAkkaActor,
    libraryDependencies += libAkkaStream,
  ) ++ GlobalSettings.commonSettings
}