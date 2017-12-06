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

import sbt.Keys._
import sbt.{Def, _}
import com.github.dnvriend.sbt.aws.AwsPlugin
import com.github.dnvriend.sbt.aws.AwsPluginKeys._
import com.github.dnvriend.sbt.aws.task._
import com.github.dnvriend.sbt.sam.state.{CreateCloudFormationStack, SamState}
import com.github.dnvriend.sbt.sam.task._
import sbt.internal.inc.classpath.ClasspathUtilities
import sbtassembly.{Assembly, AssemblyPlugin}
import sbtassembly.AssemblyKeys._

object SAMPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin && AssemblyPlugin && AwsPlugin

  val autoImport = SAMPluginKeys
  import autoImport._

  override def projectSettings = Seq(
    samStage := "dev",
    samS3BucketName := s"${organization.value}-${name.value}-${samStage.value}",
    samCFTemplateName := s"${name.value}-${samStage.value}",
    samResourcePrefixName := s"${name.value}-${samStage.value}",
    samJar := (assemblyOutputPath in assembly).value,

    samProjectClassLoader := {
      val scalaInstance = Keys.scalaInstance.value
      val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
      val classDirectory: File = (Keys.classDirectory in Compile).value
      val targetDir: File = Keys.target.value
      val classpath = Seq(classDirectory, targetDir) ++ fullClasspath
      val cl: ClassLoader = ClasspathUtilities.makeLoader(classpath, scalaInstance)
      cl
    },

    discoveredClassFiles := ((compile in Compile) map DiscoverClasses.run keepAs discoveredClassFiles triggeredBy (compile in Compile)).value,

    discoveredClasses := {
      val baseDir: File = (classDirectory in Compile).value
      val projectClassFiles: Set[File] = discoveredClassFiles.value
      val classLoader: ClassLoader = samProjectClassLoader.value
      DiscoverProjectClasses.run(projectClassFiles, baseDir, classLoader)
    },
    discoveredClasses := (discoveredClasses triggeredBy discoveredClassFiles).value,
    discoveredClasses := (discoveredClasses keepAs discoveredClasses).value,

    discoveredLambdas := DiscoverLambdas.run(discoveredClasses.value),
    discoveredLambdas := (discoveredLambdas triggeredBy discoveredClasses).value,
    discoveredLambdas := (discoveredLambdas keepAs discoveredLambdas).value,

    classifiedLambdas := ClassifyLambdas.run(discoveredLambdas.value, samStage.value),
    classifiedLambdas := (classifiedLambdas triggeredBy discoveredLambdas).value,
    classifiedLambdas := (classifiedLambdas keepAs classifiedLambdas).value,

    // generate the sam cloud formation template
    samGenerateTemplate := {
      CreateSamTemplate.run(
        classifiedLambdas.value,
        iamUserInfo.value,
        credentialsAndRegion.value,
        samStage.value,
        Keys.description.?.value.getOrElse(Keys.name.value))
    },

    // validate the sam cloud formation template
    samValidate := {
      val log = streams.value.log
      val template = samGenerateTemplate.value
      val client = clientCloudFormation.value
      log.info(CloudFormationOperations.validateTemplate(template, client).bimap(t => t.getMessage, _.toString).merge)
    },

    samProjectConfiguration := {
      ProjectConfiguration.fromConfig(
        samS3BucketName.value,
        samCFTemplateName.value,
        samResourcePrefixName.value,
        samStage.value,
        credentialsAndRegion.value,
        iamUserInfo.value
      )
    },

    samInfo := {
      val projectState = Keys.state.value.get(samAttributeProjectState.key)
      val log = streams.value.log
      log.info(projectState.map(state => {
        val nextState = SamState.nextState(state)
        s"""
           |SamInfo:
           |=============
           |$state
           |NextState: $nextState
         """.stripMargin
      }).getOrElse("Unknown, please run 'determineSamState' first"))

    },

    commands += determineSamState,
    commands += createCloudFormationStack,
    commands += deleteCloudFormationStack,
    commands += uploadJar,
    commands += deleteJar,
  )

  lazy val determineSamState = Command.command("determineSamState") { state =>
    val extracted = Project.extract(state)
    val stackName = extracted.get(samCFTemplateName)
    val describeStackResult = CloudFormationOperations.describeStack(
      DescribeStackSettings(StackName(stackName)),
      extracted.get(clientCloudFormation)
    )
    val projectState = SamState.determineState(
      stackName,
      describeStackResult
    )
    println(projectState)
    state.put(samAttributeProjectState.key, projectState)
  }

  lazy val createCloudFormationStack = Command.command("createCloudFormationStack") { state =>
    val extracted = Project.extract(state)
    val stackName = extracted.get(samCFTemplateName)
    val (_, config) = extracted.runTask(samProjectConfiguration, state)
    println(config)
    val result = CloudFormationOperations.createStack(CreateStackSettings(
      CreateSamTemplate.fromProjectConfiguration(config),
      StackName(stackName)),
      extracted.get(clientCloudFormation)
    )
    println(result)
    state
  }

  lazy val deleteCloudFormationStack = Command.command("deleteCloudFormationStack") { state =>
    val extracted = Project.extract(state)
    val stackName = extracted.get(samCFTemplateName)
    val result = CloudFormationOperations.deleteStack(
      DeleteStackSettings(StackName(stackName)),
      extracted.get(clientCloudFormation)
    )
    println(result)
    state
  }

  lazy val uploadJar = Command.command("uploadJar") { state =>
    val extracted = Project.extract(state)
    val (_, jarFile) = extracted.runTask((assembly), state)
    val (_, config) = extracted.runTask(samProjectConfiguration, state)
    val result = S3Operations.putObject(
      PutObjectSettings(
        S3BucketId(config.samS3BucketName.value),
        S3ObjectKey(jarFile.getName),
        S3Object(jarFile)
      ),
      extracted.get(clientS3)
    )
    println(result)
    state
  }

  lazy val deleteJar = Command.command("deleteJar") { state =>
    val extracted = Project.extract(state)
    val (_, config) = extracted.runTask(samProjectConfiguration, state)
    val (_, jarFile) = extracted.runTask(assemblyOutputPath in assembly, state)
    val result = S3Operations.deleteObject(
      DeleteObjectSettings(
        S3BucketId(config.samS3BucketName.value),
        S3ObjectKey(jarFile.getName)
      ),
      extracted.get(clientS3)
    )
    println(result)
    state
  }
}