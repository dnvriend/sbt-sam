import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport.{scriptedBufferLog, scriptedLaunchOpts}

object SbtSamPluginSettings extends AutoPlugin with LibraryDependenciesKeys {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && ScriptedPlugin && LibraryDependencies

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libTypesafeConfig.value,
    libraryDependencies += libPureConfig.value,
    libraryDependencies += libCirceYaml.value,
    libraryDependencies += libPlayJson.value,
    libraryDependencies += libAwsJavaSdk.value,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    addSbtPlugin(libSbtAssembly),
  )
}
