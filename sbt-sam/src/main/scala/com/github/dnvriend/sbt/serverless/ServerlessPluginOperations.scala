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

package com.github.dnvriend.sbt.serverless

import java.lang.annotation.Annotation

import com.github.dnvriend.sbt.util.{ProgressBar, SequenceEither}
import com.typesafe.config._
import pureconfig._
import sbt._

import scala.collection.JavaConverters._
import scala.sys.process.Process

object ServerlessPluginOperations {


  def createCognitoCloudFormationFile(workDir: File, stage: String, projectName: String, pools: List[Cognito.UserPool], log: Logger): Unit = {
    val resources = pools.map { pool =>
      s"""
         |"${pool.configName}": {
         |  "Type" : "AWS::Cognito::UserPool",
         |  "Properties" : {
         |    "AdminCreateUserConfig" : {
         |      "AllowAdminCreateUserOnly": ${pool.adminCreateUserConfig.allowAdminCreateUserOnly},
         |      "UnusedAccountValidityDays": ${pool.adminCreateUserConfig.unusedAccountValidityDays}
         |    },
         |    "Policies" : {
         |      "PasswordPolicy": {
         |        "MinimumLength": ${pool.policies.passwordPolicy.minimumLength},
         |        "RequireLowercase": ${pool.policies.passwordPolicy.requireLowercase},
         |        "RequireNumbers": ${pool.policies.passwordPolicy.requireNumbers},
         |        "RequireSymbols": ${pool.policies.passwordPolicy.requireSymbols},
         |        "RequireUppercase": ${pool.policies.passwordPolicy.requireUppercase}
         |      }
         |    },
         |    "UserPoolName" : "${pool.name}_${stage.toLowerCase}"
         |  }
         |}
    """.stripMargin
    }.mkString(",\n")
    val templateContent = createCloudFormationTemplate(resources, s"User Pool for project $projectName and stage: $stage")
    log.info(s"Creating Cognito CloudFormation file in workDir: '$workDir' with contents:\n$templateContent")
    IO.write(workDir / cognitoCloudFormationTemplateName(stage), templateContent)
  }

  def runCognitoCloudFormationFile(workDir: File, stage: String, region: String, profile: String, projectName: String, log: Logger): Unit = {
    log.info(s"Running Cognito CloudFormation script for profile: '$profile', region: '$region'")
    val templateFile = workDir / cognitoCloudFormationTemplateName(stage)
    Process(s"aws cloudformation deploy --template-file ${templateFile.absolutePath} --stack-name $projectName$stage --region $region --profile $profile") ! log
  }

  def createDynamoDbCloudFormationFile(workDir: File, stage: String, projectName: String, tables: List[DynamoDb.Table], log: Logger): Unit = {
    val resources: String = tables.map { table =>
      s"""
         |"${table.configName}": {
         |  "Type" : "AWS::DynamoDB::Table",
         |  "Properties" : {
         |    "AttributeDefinitions" : [ AttributeDefinitions, ... ],
         |    "GlobalSecondaryIndexes" : [ GlobalSecondaryIndexes, ... ],
         |    "KeySchema" : [ KeySchema, ... ],
         |    "LocalSecondaryIndexes" : [ LocalSecondaryIndexes, ... ],
         |    "ProvisionedThroughput" : ProvisionedThroughput,
         |    "StreamSpecification" : StreamSpecification,
         |    "TableName" : String,
         |    "Tags" : [ Resource Tag, ... ],
         |    "TimeToLiveSpecification" : TimeToLiveSpecification
         |  }
         |}
       """.stripMargin
    }.mkString(",\n")
    val templateContent: String = createCloudFormationTemplate(resources, s"DynamoDB tables for project $projectName and stage: $stage")
    log.info(s"Creating DynamoDb CloudFormation file in workDir: '$workDir' with contents:\n$templateContent")
    //TODO: generate the template
    IO.write(workDir / dynamoDbCloudFormationTemplateName(stage), templateContent)
  }

  def runDynamoDbCloudFormationFile(workDir: File, stage: String, log: Logger): Unit = {

  }

  def showServerlessInfo(baseDirectory: File, options: Serverless.Options, log: Logger): Unit = {
    log.info(s"Getting project info for stage: '${options.stage}' of profile: '${options.profile}' of region: '${options.region}'")
    Process(s"sls info --verbose ${options.cliOptions}", baseDirectory) ! log
  }

  def setEnvironmentVariablesForTest(idToken: Option[String], serverlessInfo: Serverless.Info, options: Serverless.Options, log: Logger): Map[String, String] = {
    log.debug(s"Setting test environment variables for Serverless Integration Test for stage: '${options.stage}' of profile: '${options.profile}' to region: '${options.region}' with info: $serverlessInfo with tokens: $idToken")
    Map(
      SERVERLESS_PROJECT_BASE_URL -> serverlessInfo.endpoint.getOrElse("http://localhost:8080"),
    ) ++ idToken.map(token => SERVERLESS_ID_TOKEN -> token)
  }

  def removeService(baseDirectory: File, options: Serverless.Options)(implicit log: Logger): Unit = {
    log.info(s"Removing serverless project from stage: '${options.stage}' of profile: '${options.profile}' of region: '${options.region}'")
    Process(s"sls remove ${options.cliOptions}", baseDirectory).lineStream_!(log).foreach {
      case line if line contains "Getting all objects in S3 bucket" => ProgressBar.show(16 * 1)
      case line if line contains "Removing objects in S3 bucket" => ProgressBar.show(16 * 2)
      case line if line contains "Removing Stack" => ProgressBar.show(16 * 3)
      case line if line contains "Checking Stack removal progress" => ProgressBar.show(16 * 4)
      case line if line contains "Stack removal finished" => ProgressBar.show(16 * 5)
      case line if line contains "Successfully archived" => ProgressBar.show(100)
      case line if line contains "Serverless Error" => ProgressBar.show(100)
      case line if line contains "does not exist" => println("Error: " + line.split("does not exist").take(1).headOption.getOrElse("") + " does not exist")
      case _ =>
    }
  }

  def getServerlessInfo(slsVersion: String, baseDirectory: File, options: Serverless.Options, log: Logger): Serverless.Info = {
    log.info(s"Getting Serverless info for stage: '${options.stage}' of profile: '${options.profile}' to region: '${options.region}'")

    val serviceInfo: String = Process(s"sls info --verbose ${options.cliOptions}", baseDirectory)
      .lineStream_!(log)
      .dropWhile(_ != "Stack Outputs")
      .takeWhile(_ != "\n")
      .toList
      .mkString("\n")

    val result = serviceInfo
      .split("\n")
      .toList
      .drop(1)

    val lambdas: List[Serverless.LambdaInfo] = result
      .filter(_.contains("LambdaFunctionQualifiedArn"))
      .map(_.split(": "))
      .map(xs => Serverless.LambdaInfo(xs.head.replace("LambdaFunctionQualifiedArn", ""), xs.drop(1).head))

    val serviceEndpointOption: Option[String] = result.find(_.contains("ServiceEndpoint"))
    val endpointUrl: Option[String] = serviceEndpointOption.flatMap(_.split(": ").drop(1).headOption)
    val slsBucketNameOption: Option[String] = result.find(_.contains("ServerlessDeploymentBucketName"))
    val slsBucketName: Option[String] = slsBucketNameOption.flatMap(_.split(": ").drop(1).headOption)

    Serverless.Info(slsVersion.trim(), endpointUrl, lambdas, slsBucketName)
  }

  def getServerlessVersion(log: Logger): String = {
    Process(s"sls --version").!!(log)
  }

  def deployLambda(lambdaName: String, baseDirectory: File, options: Serverless.Options, log: Logger): Unit = {
    log.info(s"Deploying lambda '$lambdaName' to stage: '${options.stage}' of profile: '${options.profile}' of region: '${options.region}'")
    Process(s"sls deploy -f $lambdaName ${options.cliOptions}", baseDirectory) ! log
  }

  def deployService(baseDirectory: File, options: Serverless.Options)(implicit log: Logger): Unit = {
    log.info(s"Deploying serverless project to stage: '${options.stage}' of profile: '${options.profile}' of region: '${options.region}'")
    Process(s"sls deploy --verbose ${options.cliOptions}", baseDirectory).lineStream_!(log).foreach {
      case line if line contains "Packaging service" => ProgressBar.show(16 * 1)
      case line if line contains "Uploading CloudFormation file to S3" => ProgressBar.show(16 * 2)
      case line if line contains "Uploading artifacts" => ProgressBar.show(16 * 3)
      case line if line contains "Validating template" => ProgressBar.show(16 * 4)
      case line if line contains "Updating Stack" => ProgressBar.show(16 * 5)
      case line if line contains "Checking Stack update progress" => ProgressBar.show(16 * 6)
      case line if line contains "Stack update finished" => ProgressBar.show(100)
      case line if line contains "Serverless Error" => ProgressBar.show(100)
      case line if line contains "Operation failed" => println("Deployment failed")
      case line if line contains "An error occurred" => println("Error: " + line.split("An error occurred:").drop(1).headOption.getOrElse(""))
      case _ =>
    }
  }

  def dynamoDbTables(baseDir: File, log: Logger): List[DynamoDb.Table] = {
    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "serverless.conf")
    val dynamodb = conf.getConfig("dynamodb")
    val result = dynamodb.root().keySet().asScala.toList.map(name => (name, dynamodb.getConfig(name))).map { case (name, conf) =>
      loadConfig[DynamoDb.Table](conf).map(_.copy(configName = name))
    }
    SequenceEither.sequence(result).getOrElse(Nil)
  }

  def cognitoUserPools(baseDir: File, log: Logger): List[Cognito.UserPool] = {
    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "serverless.conf")
    val dynamodb = conf.getConfig("cognito")
    val result = dynamodb.root().keySet().asScala.toList.map(name => (name, dynamodb.getConfig(name))).map { case (name, conf) =>
      loadConfig[Cognito.UserPool](conf).map(_.copy(configName = name))
    }
    SequenceEither.sequence(result).getOrElse(Nil)
  }

  def getLambdas(classDir: File, stage: String, cl: ClassLoader, log: Logger): List[Lambda.LambdaHandler] = {
    import scala.tools.nsc.classpath._
    val allClassFilesInClassDir: Seq[File] = (classDir ** "*.class").get
    val relativizer = IO.relativize(classDir, _: File)
    val xs: Seq[Class[_]] = allClassFilesInClassDir
      .flatMap(relativizer(_).toSeq)
      .map(FileUtils.stripClassExtension)
      .filterNot(_.contains("$"))
      .map(_.replace("/", "."))
      .map(cl.loadClass)

    def annotationPredicate(annotationName: String)(cl: Class[_]): Boolean = {
      cl.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains(annotationName))
    }

    def mapAnnoToDynamoHandler(fqcn: String, simpleName: String, anno: Annotation): Lambda.DynamoHandler = {
      val tableName = anno.annotationType().getMethod("tableName").invoke(anno).asInstanceOf[String]
      val batchSize = anno.annotationType().getMethod("batchSize").invoke(anno).asInstanceOf[Int]
      val startingPosition = anno.annotationType().getMethod("startingPosition").invoke(anno).asInstanceOf[String]
      val enabled = anno.annotationType().getMethod("enabled").invoke(anno).asInstanceOf[Boolean]
      val name = anno.annotationType().getMethod("name").invoke(anno).asInstanceOf[String]
      val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
      val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
      val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]
      Lambda.DynamoHandler(fqcn, simpleName, stage, tableName, batchSize, startingPosition, enabled, name, memorySize, timeout, description)
    }

    def mapAnnoToHttpHandler(className: String, simpleName: String, anno: Annotation): Lambda.HttpHandler = {
      val path = anno.annotationType().getMethod("path").invoke(anno).asInstanceOf[String]
      val method = anno.annotationType().getMethod("method").invoke(anno).asInstanceOf[String]
      val authorization = anno.annotationType().getMethod("authorization").invoke(anno).asInstanceOf[Boolean]
      val name = anno.annotationType().getMethod("name").invoke(anno).asInstanceOf[String]
      val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
      val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
      val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]
      Lambda.HttpHandler(className, simpleName, stage, path, method, authorization, name, memorySize, timeout, description)
    }

    val dynamoHandlers = xs.filter(annotationPredicate("DynamoHandler"))
      .map(cl => (cl.getName, cl.getSimpleName, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("DynamoHandler"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToDynamoHandler(fqcn, simpleName, anno))
      }.toList

    val httpHandlers = xs.filter(annotationPredicate("HttpHandler"))
      .map(cl => (cl.getName, cl.getSimpleName, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("HttpHandler"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToHttpHandler(fqcn, simpleName, anno))
      }.toList

    dynamoHandlers ++ httpHandlers
  }

  def createServerlessYaml(config: ProjectConfiguration,
                           projectName: String,
                           options: Serverless.Options,
                           artifactJarToPackage: String,
                           awsAccountId: String,
                           userPoolId: String,
                           baseDirectory: File, log: Logger): Unit = {

    import _root_.io.circe.yaml._
    import cats.syntax.either._
    import play.api.libs.json._

    val lambdaJson = config.lambdas.map {
      case Lambda.HttpHandler(fqcn, simpleName, stage, path, method, authorization, name, memorySize, timeout, description) =>
        val lambdaName = simpleName + stage.toLowerCase.capitalize
        Json.obj(
          lambdaName -> Json.obj(
            "handler" -> s"$fqcn::handleRequest",
            "name" -> name,
            "description" -> description,
            "memorySize" -> memorySize,
            "timeout" -> timeout,
            "events" -> Json.arr(
              Json.obj(
                "http" -> Json.obj(
                  "path" -> path,
                  "method" -> method,
                  "authorizer" -> Json.obj(
                    "arn" -> s"arn:aws:cognito-idp:${options.region}:$awsAccountId:userpool/$userPoolId"
                  )
                )
              )
            )
          )
        )
      case Lambda.DynamoHandler(fqcn, simpleName, stage, tableName, batchSize, startingPosition, enabled, name, memorySize, timeout, description, streamArn) =>
        val lambdaName = simpleName + stage.toLowerCase.capitalize
        val arn: String = streamArn.getOrElse("No dynamodb streams Arn found")
        Json.obj(
          lambdaName -> Json.obj(
            "handler" -> s"$fqcn::handleRequest",
            "name" -> name,
            "description" -> description,
            "memorySize" -> memorySize,
            "timeout" -> timeout,
            "events" -> Json.arr(
              Json.obj(
                "stream" -> Json.obj(
                  "arn" -> arn,
                  "type" -> "dynamodb",
                  "batchSize" -> batchSize,
                  "startingPosition" -> startingPosition,
                  "enabled" -> enabled
                )
              )
            )
          )
        )
    }.reduce(_ ++ _)

    val jsonString: String = Json.obj(
      "service" -> projectName,
      "provider" -> Json.obj(
        "name" -> "aws",
        "runtime" -> "java8",
        "stage" -> options.stage,
        "region" -> options.region,
        "timeout" -> 20
      ),
      "package" -> Json.obj(
        "artifact" -> artifactJarToPackage
      ),
      "functions" -> lambdaJson
    ).toString

    val jsonAST = _root_.io.circe.parser.parse(jsonString).valueOr(throw _)
    val yamlPretty: String = _root_.io.circe.yaml.Printer(dropNullKeys = true,
      mappingStyle = Printer.FlowStyle.Block)
      .pretty(jsonAST)

    //TODO: overwrite serverless.yaml in basedir

    println(yamlPretty)
  }
}