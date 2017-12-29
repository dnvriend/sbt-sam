import LibraryDependencies._
import sbt.Keys._
import sbt._

object SamTestingLibSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin
  override def projectSettings = GlobalSettings.commonSettings
}