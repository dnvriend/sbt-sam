import LibraryDependencies._
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoOption, BuildInfoPlugin}

object SbtSamSchemaPluginSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && BuildInfoPlugin

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libPlayJson,
    libraryDependencies += libAwsJavaSdk,
    libraryDependencies += libAvro4s,
    libraryDependencies += libAvro,
    libraryDependencies += libAvroCompiler,
    libraryDependencies += libScalajHttp,
    libraryDependencies += libScalazScalaTest % Test,
    libraryDependencies += libScalaTest % Test,

    addSbtPlugin(libSbtAssembly),
  ) ++
    buildInfoSettings

  lazy val buildInfoSettings = Seq(
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "avro4s",
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime
  )
}
