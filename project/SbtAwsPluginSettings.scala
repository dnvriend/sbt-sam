import LibraryDependencies._
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoOption, BuildInfoPlugin}

object SbtAwsPluginSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && BuildInfoPlugin

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libPlayJson,
    libraryDependencies += libAwsJavaSdk,
    libraryDependencies += libScalazScalaTest % Test,
    libraryDependencies += libScalaTest % Test,
  ) ++
    buildInfoSettings

  lazy val buildInfoSettings = Seq(
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.dnvriend.sbt.aws",
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime
  )
}
