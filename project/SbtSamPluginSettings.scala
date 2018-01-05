import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport.{scriptedBufferLog, scriptedLaunchOpts}
import LibraryDependencies._
import sbtbuildinfo._
import sbtbuildinfo.BuildInfoKeys._

object SbtSamPluginSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && ScriptedPlugin

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libTypesafeConfig,
    libraryDependencies += libPureConfig,
    libraryDependencies += libPlayJson,
    libraryDependencies += libAwsJavaSdk,
    libraryDependencies += libScalazScalaTest,
    libraryDependencies += libScalaTest,

    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    addSbtPlugin(libSbtAssembly),
  ) ++ buildInfoSettings

  lazy val buildInfoSettings = Seq(
    buildInfoObject := "SbtSamPluginBuildInfo",
    buildInfoPackage := "com.github.dnvriend.sbt.sam",
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToMap
  )
}
