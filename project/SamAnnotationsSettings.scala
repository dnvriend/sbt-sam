import sbt.Keys._
import sbt._

object SamAnnotationsSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
  )

}