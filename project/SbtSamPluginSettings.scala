import LibraryDependencies._
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys._
import sbtbuildinfo._

object SbtSamPluginSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libTypesafeConfig,
    libraryDependencies += libPureConfig,
    libraryDependencies += libPlayJson,
    libraryDependencies += libAwsJavaSdk,
    libraryDependencies += libScalazScalaTest,
    libraryDependencies += libScalaTest,

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
