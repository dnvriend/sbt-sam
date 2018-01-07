import LibraryDependencies._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport.{scriptedBufferLog, scriptedLaunchOpts}
import sbt._

object SbtSamSchemaPluginSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && ScriptedPlugin

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libPlayJson,
    libraryDependencies += libAwsJavaSdk,
    libraryDependencies += libScalazScalaTest % Test,
    libraryDependencies += libScalaTest % Test,

    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    addSbtPlugin(libSbtAssembly),
  )
}
