package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.{AmazonUser, CredentialsAndRegion, TemplateBody}
import play.api.libs.json.{JsObject, JsValue, Json}

import scalaz.Monoid
import scalaz._
import scalaz.Scalaz._

object CreateSamTemplate {
  implicit val JsObjMonoid: Monoid[JsObject] = Monoid.instance(_ ++ _, Json.obj())

  def run(lambdas: Set[LambdaHandler],
          user: AmazonUser,
          credentialsAndRegion: CredentialsAndRegion,
          stage: String,
          description: String): TemplateBody = {
    TemplateBody(createSamTemplate(createLambdaResources(lambdas, user, credentialsAndRegion), description))
  }

  def createSamTemplate(resources: JsValue, description: String = ""): String = {
    Json.obj(
      "AWSTemplateFormatVersion" -> "2010-09-09",
      "Transform" -> "AWS::Serverless-2016-10-31",
      "Description" -> description,
      "Globals" -> Json.obj(
        "Runtime" ->"java8",
        "Timeout" -> 300,
      ),
      "Resources" -> resources
    ).toString
  }

  def createLambdaResources(lambdas: Set[LambdaHandler],
                            user: AmazonUser,
                            credentialsAndRegion: CredentialsAndRegion): JsValue = {
    lambdas.foldMap {
      case HttpHandler(fqcn, simpleName, stage, path, method, authorization, name, memorySize, timeout, description) =>
        val lambdaName = simpleName + stage.toLowerCase.capitalize
        val userPoolId = ""
        val awsAccountId = user.arn.accountId.value
        functionTemplate(lambdaName, fqcn, name, description, memorySize, timeout) {
          Json.obj(
            s"${name}Api" -> Json.obj(
              "Type" -> "Api",
              "Properties" -> Json.obj(
                "Path" -> path,
                "Method" -> method,
              )
            )
          )
        }

      case DynamoHandler(fqcn, simpleName, stage, tableName, batchSize, startingPosition, enabled, name, memorySize, timeout, description, streamArn) =>
        val lambdaName = simpleName + stage.toLowerCase.capitalize
        val arn: String = streamArn.getOrElse("No dynamodb streams Arn found")
        functionTemplate(lambdaName, fqcn, name, description, memorySize, timeout) {
            Json.obj(
              s"${name}DynamoDB" -> Json.obj(
                "Type" -> "DynamoDB",
                "Properties" -> Json.obj(
                  "Stream" -> arn,
                  "StartingPosition" -> startingPosition,
                  "BatchSize" -> batchSize
                )
              )
            )
        }
    }
  }

  def functionTemplate(lambdaName: String,
                       fqcn: String,
                       name: String,
                       description: String,
                       memorySize: Int,
                       timeout: Int)(event: JsObject): JsObject = {
    Json.obj(
      lambdaName -> Json.obj(
        "Type" -> "AWS::Serverless::Function",
        "Properties" -> Json.obj(
          "Handler" -> s"$fqcn::handleRequest",
          "CodeUri" -> "",
          "FunctionName" -> name,
          "Description" -> description,
          "MemorySize" -> memorySize,
          "Timeout" -> timeout,
          "Role" -> "",
          "Policies" -> Json.arr(),
          "Tracing" -> name,
          "Events" -> event
        )
      )
    )
  }
}
