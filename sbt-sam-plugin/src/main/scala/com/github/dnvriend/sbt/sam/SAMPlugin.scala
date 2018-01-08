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
import com.github.dnvriend.sbt.aws.AwsPluginKeys._
import com.github.dnvriend.sbt.aws.task._
import com.github.dnvriend.sbt.sam.task._
import com.github.dnvriend.sbt.sam.resource.ResourceOperations
import sbt.complete.DefaultParsers._
import sbt.Keys._
import sbt._
import sbt.internal.inc.classpath.ClasspathUtilities
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin

object SAMPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin && AssemblyPlugin && AwsPlugin

  val autoImport = SAMPluginKeys

  import autoImport._

  override def projectSettings = Seq(
    samStageValue := {
      val samStageProp: Option[String] = samStage.?.value
      val samStageEnv: Option[String] = sys.env.get("SAM_STAGE")
      samStageEnv.orElse(samStageProp).getOrElse(throw new RuntimeException("'SAM_STAGE' not set in environment or 'samStage' not set in project"))
    },
    samS3BucketName := s"${organization.value}-${name.value}-${samStageValue.value}",
    samCFTemplateName := s"${organization.value}-${name.value}-${samStageValue.value}",
    samResourcePrefixName := s"${name.value}-${samStageValue.value}",
    (assemblyJarName in assembly) := "codepackage.jar",

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

    classifiedLambdas := ClassifyLambdas.run(discoveredLambdas.value, samStageValue.value),
    classifiedLambdas := (classifiedLambdas triggeredBy discoveredLambdas).value,
    classifiedLambdas := (classifiedLambdas keepAs classifiedLambdas).value,

    discoveredSqlFiles := {
      ((Keys.unmanagedSources in Compile).value ** "*.sql").get.toSet
    },

    classifiedSqlFiles := {
      ClassifySqlFiles.run(discoveredSqlFiles.value, streams.value.log)
    },

    // validate the sam cloud formation template
    samValidate := {
      val log = streams.value.log
      val config = samProjectConfiguration.value
      val client = clientCloudFormation.value
      val jarName = (assemblyJarName in assembly).value
      val s3client = clientS3.value
      val latestVersion: Option[S3ObjectVersionId] = S3Operations.latestVersion(
        ListVersionsSettings(
          BucketName(config.samS3BucketName.value),
          S3ObjectKey(jarName)
        ), s3client)
      val template: TemplateBody = CloudFormationTemplates.updateTemplate(config, jarName, latestVersion.map(_.value).getOrElse("NO_ARTIFACT_AVAILABLE_YET"))

      log.info("validating template:")
      println(template.value)
      log.info(CloudFormationOperations.validateTemplate(template, client)
        .bimap(t => t.getMessage, _.toString).merge)
    },

    dynamoDbTableResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveDynamoDbTables(config)
    },

    iamRolesResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveRoles(config)
    },

    topicResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveTopics(config)
    },

    streamResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveStreams(config)
    },

    cognitoResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveAuthPool(config)
    },

    importAuthpool := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      val authpool = cognitoResources.value

      ResourceOperations.retrieveImportAuthPool(config, authpool)
    },

    bucketResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveBuckets(config)
    },

    s3FirehoseResources := {
      val baseDir: File = baseDirectory.value
      val config = ResourceOperations.readConfig(baseDir)
      ResourceOperations.retrieveS3Firehose(config)
    },

    samProjectConfiguration := {
      ProjectConfiguration.fromConfig(
        name.value,
        version.value,
        description.value,
        samS3BucketName.value,
        samCFTemplateName.value,
        samResourcePrefixName.value,
        samStageValue.value,
        iamCredentialsRegionAndUser.value,
        iamUserInfo.value,
        SamResources(
          cognitoResources.value,
          importAuthpool.value,
          classifiedLambdas.value,
          dynamoDbTableResources.value,
          topicResources.value,
          streamResources.value,
          bucketResources.value,
          s3FirehoseResources.value,
          iamRolesResources.value,
          classifiedSqlFiles.value,
        )
      )
    },
    samProjectConfiguration := (samProjectConfiguration keepAs samProjectConfiguration).value,

    samInfo := {
      CloudFormationStackInfo.run(
        samProjectConfiguration.value,
        samDescribeCloudFormationStack.value,
        clientCloudFormation.value,
        clientDynamoDb.value,
        clientSns.value,
        clientKinesis.value,
        clientAwsLambda.value,
        clientS3.value,
        clientIam.value,
        clientCognito.value,
        clientKinesisFirehose.value,
        streams.value.log
      )
    },

    samServiceEndpoint := {
      val logger = streams.value.log
      val stack = samDescribeCloudFormationStack.value
      stack.map(SamStack.fromStack).flatMap(_.serviceEndpoint)
    },

    samUploadArtifact := {
      ArtifactUpload.run(
        samProjectConfiguration.value,
        assembly.value,
        clientS3.value,
        streams.value.log
      )
    },

    samDeleteArtifact := {
      ArtifactDelete.run(
        samProjectConfiguration.value,
        (assemblyOutputPath in assembly).value,
        clientS3.value,
        streams.value.log
      )
    },

    samDeleteCloudFormationStack := {
      CloudFormationStackDelete.run(
        samProjectConfiguration.value,
        samDescribeCloudFormationStack.value,
        clientCloudFormation.value,
        streams.value.log
      )
    },

    samCreateCloudFormationStack := {
      CloudFormationStackCreate.run(
        samProjectConfiguration.value,
        samDescribeCloudFormationStackForCreate.value,
        clientCloudFormation.value,
        streams.value.log
      )
    },

    samUpdateCloudFormationStack := {
      CloudFormationStackUpdate.run(
        samProjectConfiguration.value,
        samDescribeCloudFormationStack.value,
        clientCloudFormation.value,
        (assemblyJarName in assembly).value,
        clientS3.value,
        streams.value.log
      )
    },

    samDescribeCloudFormationStackForCreate := {
      val config: ProjectConfiguration = samProjectConfiguration.value
      CloudFormationOperations.getStack(
        DescribeStackSettings(StackName(config.samCFTemplateName.value)),
        clientCloudFormation.value
      )
    },

    samDescribeCloudFormationStack := {
      val config: ProjectConfiguration = samProjectConfiguration.value
      CloudFormationOperations.getStack(
        DescribeStackSettings(StackName(config.samCFTemplateName.value)),
        clientCloudFormation.value
      )
    },

    samCreateUsers := {
      CreateCognitoUsers.run(
        samProjectConfiguration.value,
        clientCognito.value,
        streams.value.log
      )
    },

    samCreateUserToken := {
      CreateCognitoUsers.getIdToken(
        samProjectConfiguration.value,
        clientCognito.value,
        streams.value.log
      )
    },

    samLogs := {
      val lambdaName = Defaults.getForParser(samProjectConfiguration)((state, config) => {
        val strings: List[String] = config.toList.flatMap(_.lambdas.toList).map(_.lambdaConfig.simpleClassName)
        Space ~> StringBasic.examples(strings: _*)
      }).parsed
      val logger = streams.value.log
      val config = samProjectConfiguration.value
      val lambdaClient = clientAwsLambda.value
      val logsClient = clientAwsLogs.value
      val projectName = config.projectName
      val stage = config.samStage.value
      val maybeLambdaConfig = config.lambdas.find(_.lambdaConfig.simpleClassName == lambdaName).map(_.lambdaConfig)
      for {
        lambdaConf <- maybeLambdaConfig
        function <- AwsLambdaOperations.findFunction(lambdaConf.fqcn, projectName, stage, lambdaClient)
        logGroup <- CloudWatchLogsOperations.findLogGroup(function.getFunctionName, logsClient)
      } {
        CloudWatchLogsOperations.getLogEvents(logGroup.getLogGroupName, logsClient).sortBy(_.timestamp).foreach {
          case LogEvent(timestamp, ingestionTime, message) =>
            def format(time: Long): String = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new java.util.Date(time))
            logger.info(s"${format(timestamp)} - $message")
        }
      }
    },

    samRemove := Def.sequential(samDeleteArtifact, samDeleteCloudFormationStack).value,
    samDeploy := Def.sequential(samCreateCloudFormationStack, samUploadArtifact, samUpdateCloudFormationStack).value,
  )
}