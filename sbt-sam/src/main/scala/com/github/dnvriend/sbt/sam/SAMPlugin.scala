// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.sbt.sam

import com.github.dnvriend.sbt.aws.AwsPlugin
import sbt.Keys._
import sbt.{ Def, _ }
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin

object SAMPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = AssemblyPlugin && AwsPlugin

  lazy val ItTest: Configuration = config("it") extend Test

  lazy val startupTransition: State => State = { s: State =>
    "initServerlessPlugin" :: s
  }

  val autoImport = SAMPluginKeys

  //  import autoImport._

  //  lazy val defaultSettings: Seq[Setting[_]] = inConfig(ItTest)(Defaults.testTasks) ++ Seq(
  //    onLoad in Global := {
  //      val old = (onLoad in Global).value
  //      startupTransition compose old
  //    },
  //
  //    initServerlessPlugin := {
  //      streams.value.log.info("Creating serverless work dir")
  //      IO.createDirectory(target.value / SERVERLESS_PLUGIN_WORK_DIR)
  //    },
  //
  //    samOptions := {
  //      Serverless.Options(
  //        stage = samStage.?.value.getOrElse(DEFAULT_STAGE),
  //        profile = "", //awsProfile.value,
  //        region = "", //awsRegion.value
  //      )
  //    },
  //    samDeploy := ServerlessPluginOperations.deployService(baseDirectory.value, samOptions.value)(streams.value.log),
  //    samDeploy := (samDeploy dependsOn assembly dependsOn clean).value,
  //
  //    samDeployLambda := {
  //      val lambdaName: String = Def.spaceDelimited("lambda name").parsed.toList.head
  //      ServerlessPluginOperations.deployLambda(lambdaName, baseDirectory.value, samOptions.value, streams.value.log)
  //    },
  //    samDeployLambda := (samDeployLambda dependsOn assembly dependsOn clean).evaluated,
  //    samRemove := ServerlessPluginOperations.removeService(baseDirectory.value, samOptions.value)(streams.value.log),
  //    samVersion := ServerlessPluginOperations.getServerlessVersion(streams.value.log),
  //    samInfo := ServerlessPluginOperations.showServerlessInfo(baseDirectory.value, samOptions.value, streams.value.log),
  //    samGetInfo := ServerlessPluginOperations.getServerlessInfo(samVersion.value, baseDirectory.value, samOptions.value, streams.value.log),
  //    samGetInfo := samGetInfo.keepAs(samGetInfo).triggeredBy(samInfo).value,
  //    parallelExecution in ItTest := false,
  //    testOptions in Test := Seq(Tests.Filter(Filters.unitFilter)),
  //    testOptions in ItTest := Seq(Tests.Filter(Filters.itFilter)),
  //    fork in Test := true,
  //    fork in ItTest := true,
  //    envVars in Test := ServerlessPluginOperations.setEnvironmentVariablesForTest(awsGetCognitoTokens.value.map(_.idToken), samGetInfo.value, samOptions.value, streams.value.log),
  //    envVars in ItTest := envVars.value,
  //
  //    // serverless project tasks
  //    samFullClassLoader := {
  //      val scalaInstance = Keys.scalaInstance.value
  //      val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
  //      val classDirectory: File = (Keys.classDirectory in Compile).value
  //      val targetDir: File = Keys.target.value
  //      val classpath = Seq(classDirectory, targetDir) ++ fullClasspath
  //      val cl: ClassLoader = sbt.internal.inc.classpath.ClasspathUtilities.makeLoader(classpath, scalaInstance)
  //      cl
  //    },
  //    samProjectConfiguration := {
  //      val lambdas = ServerlessPluginOperations.getLambdas((classDirectory in Compile).value, samStage.value, samFullClassLoader.value, streams.value.log)
  //      val tables = ServerlessPluginOperations.dynamoDbTables(baseDirectory.value, streams.value.log)
  //      val userPools = ServerlessPluginOperations.cognitoUserPools(baseDirectory.value, streams.value.log)
  //
  //      ProjectConfiguration(lambdas, tables, userPools)
  //    },
  //    samCreateServerlessYaml := {
  //      val assemblyFullPath: File = (assemblyOutputPath in assembly).value
  //      val baseDir: File = baseDirectory.value
  //      val assemblyRelativePath: String = IO.relativize(baseDir, assemblyFullPath).getOrElse("")
  //      val projectConfiguration = samProjectConfiguration.value
  //      val allTablesDescribed = awsDescribeTables.value
  //      val httpHandlers = projectConfiguration.lambdas.filter(_.isInstanceOf[Lambda.HttpHandler])
  //      val dynamoHandlers = projectConfiguration.lambdas.collect({ case x: Lambda.DynamoHandler => x })
  //      val dynamoHandlersWithArn = dynamoHandlers.flatMap { handler =>
  //        allTablesDescribed.find(_.name == handler.tableName).map { table =>
  //          handler.copy(streamArn = table.stream.map(_.arn))
  //        }
  //      }
  //
  //      ServerlessPluginOperations.createServerlessYaml(
  //        projectConfiguration.copy(lambdas = httpHandlers ++ dynamoHandlersWithArn),
  //        name.value,
  //        samOptions.value,
  //        assemblyRelativePath,
  //        awsAccountId.value,
  //        awsCognitoUserPoolId.value,
  //        baseDirectory.value,
  //        streams.value.log
  //      )
  //    },
  //
  //    samCreateCloudFormationCognitoTemplate := {
  //      ServerlessPluginOperations.createCognitoCloudFormationFile(target.value / SERVERLESS_PLUGIN_WORK_DIR, samStage.value, name.value, samProjectConfiguration.value.userPools, streams.value.log)
  //    },
  //
  //    samCreateCloudFormationDynamoDbTemplate := {
  //
  //    },
  //    samRunCloudFormationCognitoTemplate := {
  //      val options = samOptions.value
  //      ServerlessPluginOperations.runCognitoCloudFormationFile(target.value / SERVERLESS_PLUGIN_WORK_DIR, options.stage, options.region, options.profile, name.value, streams.value.log)
  //    },
  //    samRunCloudFormationCognitoTemplate := (samRunCloudFormationCognitoTemplate dependsOn samCreateCloudFormationCognitoTemplate).value,
  //    samRunCloudFormationDynamoDbTemplate := {
  //
  //    },

  //  )

  override def projectSettings: Seq[Def.Setting[_]] = Seq.empty

  override def projectConfigurations: Seq[Configuration] = {
    Seq(ItTest)
  }
}