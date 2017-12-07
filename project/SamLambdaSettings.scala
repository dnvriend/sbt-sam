import sbt._
import sbt.Keys._
import LibraryDependencies._

object SamLambdaSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    libraryDependencies += libAwsLambdaJavaCore,
    libraryDependencies += libAwsLambdaJavaEvents,
    libraryDependencies += libTypesafeConfig,
    libraryDependencies += libPureConfig,
    libraryDependencies += libScalaz,
    libraryDependencies += libAvro4s,
    libraryDependencies += libCirceYaml,
    libraryDependencies += libPlayJson,
    libraryDependencies += libScalazScalaTest % Test,
    libraryDependencies += libScalaTest % Test
  )
}