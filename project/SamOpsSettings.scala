import sbt.Keys._
import sbt._

object SamOpsSettings extends AutoPlugin with LibraryDependenciesKeys {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && LibraryDependencies

  override def projectSettings = Seq(
    libraryDependencies += libAwsLambdaJavaCore.value,
    libraryDependencies += libAwsLambdaJavaEvents.value,
    libraryDependencies += libAwsJavaSdkDynamoDb.value,
    libraryDependencies += libTypesafeConfig.value,
    libraryDependencies += libPureConfig.value,
    libraryDependencies += libScalaz.value,
    libraryDependencies += libAvro4s.value,
    libraryDependencies += libSimulacrum.value,
    libraryDependencies += libCirceYaml.value,
    libraryDependencies += libPlayJson.value,
    libraryDependencies += libScalajHttp.value,
    libraryDependencies += libSbtIO.value,
    libraryDependencies += libScalazScalaTest.value % Test,
    libraryDependencies += libScalaTest.value % Test,
  )
}