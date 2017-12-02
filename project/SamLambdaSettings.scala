import sbt._
import sbt.Keys._

object SamLambdaSettings extends AutoPlugin with LibraryDependenciesKeys {
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
    libraryDependencies += libScalazScalatest.value % Test,
    libraryDependencies += libScalaTest.value % Test,
  )
}