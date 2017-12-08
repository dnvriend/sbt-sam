import sbt.Keys._
import sbt._
import LibraryDependencies._

object SamMacrosSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(

  )
}