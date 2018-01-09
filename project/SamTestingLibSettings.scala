import LibraryDependencies._
import sbt.Keys._
import sbt._

object SamTestingLibSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin
  override def projectSettings = GlobalSettings.commonSettings ++ Seq(
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
  )
}