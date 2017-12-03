//// Copyright 2017 Dennis Vriend
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
////     http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
//
//package com.github.dnvriend.sbt.sam
//
//import java.lang.annotation.Annotation
//
//import com.github.dnvriend.sbt.util.SequenceEither
//import com.typesafe.config._
//import pureconfig._
//import sbt._
//
//import scala.collection.JavaConverters._
//
//object SAMPluginOperations {
//
//  def setEnvironmentVariablesForTest(idToken: Option[String], serverlessInfo: Serverless.Info, options: Serverless.Options, log: Logger): Map[String, String] = {
//    log.debug(s"Setting test environment variables for Serverless Integration Test for stage: '${options.stage}' of profile: '${options.profile}' to region: '${options.region}' with info: $serverlessInfo with tokens: $idToken")
//    Map(
//      SAM_PROJECT_BASE_URL -> serverlessInfo.endpoint.getOrElse("http://localhost:8080"),
//    ) ++ idToken.map(token => ID_TOKEN -> token)
//  }
//
//  def dynamoDbTables(baseDir: File, log: Logger): List[DynamoDb.Table] = {
//    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "serverless.conf")
//    val dynamodb = conf.getConfig("dynamodb")
//    val result = dynamodb.root().keySet().asScala.toList.map(name => (name, dynamodb.getConfig(name))).map { case (name, conf) =>
//      loadConfig[DynamoDb.Table](conf).map(_.copy(configName = name))
//    }
//    SequenceEither.sequence(result).getOrElse(Nil)
//  }
//
//  def cognitoUserPools(baseDir: File, log: Logger): List[Cognito.UserPool] = {
//    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "serverless.conf")
//    val dynamodb = conf.getConfig("cognito")
//    val result = dynamodb.root().keySet().asScala.toList.map(name => (name, dynamodb.getConfig(name))).map { case (name, conf) =>
//      loadConfig[Cognito.UserPool](conf).map(_.copy(configName = name))
//    }
//    SequenceEither.sequence(result).getOrElse(Nil)
//  }
//
//  def getLambdas(classDir: File, stage: String, cl: ClassLoader, log: Logger): List[Lambda.LambdaHandler] = {
//    import scala.tools.nsc.classpath._
//    val allClassFilesInClassDir: Seq[File] = (classDir ** "*.class").get
//    val relativizer = IO.relativize(classDir, _: File)
//    val xs: Seq[Class[_]] = allClassFilesInClassDir
//      .flatMap(relativizer(_).toSeq)
//      .map(FileUtils.stripClassExtension)
//      .filterNot(_.contains("$"))
//      .map(_.replace("/", "."))
//      .map(cl.loadClass)
//
//    def annotationPredicate(annotationName: String)(cl: Class[_]): Boolean = {
//      cl.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains(annotationName))
//    }
//
//    def mapAnnoToDynamoHandler(fqcn: String, simpleName: String, anno: Annotation): Lambda.DynamoHandler = {
//      val tableName = anno.annotationType().getMethod("tableName").invoke(anno).asInstanceOf[String]
//      val batchSize = anno.annotationType().getMethod("batchSize").invoke(anno).asInstanceOf[Int]
//      val startingPosition = anno.annotationType().getMethod("startingPosition").invoke(anno).asInstanceOf[String]
//      val enabled = anno.annotationType().getMethod("enabled").invoke(anno).asInstanceOf[Boolean]
//      val name = anno.annotationType().getMethod("name").invoke(anno).asInstanceOf[String]
//      val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
//      val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
//      val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]
//      Lambda.DynamoHandler(fqcn, simpleName, stage, tableName, batchSize, startingPosition, enabled, name, memorySize, timeout, description)
//    }
//
//    def mapAnnoToHttpHandler(className: String, simpleName: String, anno: Annotation): Lambda.HttpHandler = {
//      val path = anno.annotationType().getMethod("path").invoke(anno).asInstanceOf[String]
//      val method = anno.annotationType().getMethod("method").invoke(anno).asInstanceOf[String]
//      val authorization = anno.annotationType().getMethod("authorization").invoke(anno).asInstanceOf[Boolean]
//      val name = anno.annotationType().getMethod("name").invoke(anno).asInstanceOf[String]
//      val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
//      val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
//      val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]
//      Lambda.HttpHandler(className, simpleName, stage, path, method, authorization, name, memorySize, timeout, description)
//    }
//
//    val dynamoHandlers = xs.filter(annotationPredicate("DynamoHandler"))
//      .map(cl => (cl.getName, cl.getSimpleName, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("DynamoHandler"))))
//      .flatMap {
//        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToDynamoHandler(fqcn, simpleName, anno))
//      }.toList
//
//    val httpHandlers = xs.filter(annotationPredicate("HttpHandler"))
//      .map(cl => (cl.getName, cl.getSimpleName, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("HttpHandler"))))
//      .flatMap {
//        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToHttpHandler(fqcn, simpleName, anno))
//      }.toList
//
//    dynamoHandlers ++ httpHandlers
//  }
//
//  def createServerlessYaml(config: ProjectConfiguration,
//                           projectName: String,
//                           options: Serverless.Options,
//                           artifactJarToPackage: String,
//                           awsAccountId: String,
//                           userPoolId: String,
//                           baseDirectory: File, log: Logger): Unit = {
//
//    import _root_.io.circe.yaml._
//    import cats.syntax.either._
//    import play.api.libs.json._
//
//    val lambdaJson = config.lambdas.map {
//      case Lambda.HttpHandler(fqcn, simpleName, stage, path, method, authorization, name, memorySize, timeout, description) =>
//        val lambdaName = simpleName + stage.toLowerCase.capitalize
//        Json.obj(
//          lambdaName -> Json.obj(
//            "handler" -> s"$fqcn::handleRequest",
//            "name" -> name,
//            "description" -> description,
//            "memorySize" -> memorySize,
//            "timeout" -> timeout,
//            "events" -> Json.arr(
//              Json.obj(
//                "http" -> Json.obj(
//                  "path" -> path,
//                  "method" -> method,
//                  "authorizer" -> Json.obj(
//                    "arn" -> s"arn:aws:cognito-idp:${options.region}:$awsAccountId:userpool/$userPoolId"
//                  )
//                )
//              )
//            )
//          )
//        )
//      case Lambda.DynamoHandler(fqcn, simpleName, stage, tableName, batchSize, startingPosition, enabled, name, memorySize, timeout, description, streamArn) =>
//        val lambdaName = simpleName + stage.toLowerCase.capitalize
//        val arn: String = streamArn.getOrElse("No dynamodb streams Arn found")
//        Json.obj(
//          lambdaName -> Json.obj(
//            "handler" -> s"$fqcn::handleRequest",
//            "name" -> name,
//            "description" -> description,
//            "memorySize" -> memorySize,
//            "timeout" -> timeout,
//            "events" -> Json.arr(
//              Json.obj(
//                "stream" -> Json.obj(
//                  "arn" -> arn,
//                  "type" -> "dynamodb",
//                  "batchSize" -> batchSize,
//                  "startingPosition" -> startingPosition,
//                  "enabled" -> enabled
//                )
//              )
//            )
//          )
//        )
//    }.reduce(_ ++ _)

//    val jsonString: String = Json.obj(
//      "service" -> projectName,
//      "provider" -> Json.obj(
//        "name" -> "aws",
//        "runtime" -> "java8",
//        "stage" -> options.stage,
//        "region" -> options.region,
//        "timeout" -> 20
//      ),
//      "package" -> Json.obj(
//        "artifact" -> artifactJarToPackage
//      ),
//      "functions" -> lambdaJson
//    ).toString
//
//    val jsonAST = _root_.io.circe.parser.parse(jsonString).valueOr(throw _)
//    val yamlPretty: String = _root_.io.circe.yaml.Printer(dropNullKeys = true,
//      mappingStyle = Printer.FlowStyle.Block)
//      .pretty(jsonAST)
//
//    println(yamlPretty)
//  }
//}