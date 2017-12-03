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
//                  "authorizer" -> Json.obj(
//                    "arn" -> s"arn:aws:cognito-idp:${credentialsAndRegion.region}:$awsAccountId:userpool/$userPoolId"
//                  )
                )
              )
            )
          )
        )
      case DynamoHandler(fqcn, simpleName, stage, tableName, batchSize, startingPosition, enabled, name, memorySize, timeout, description, streamArn) =>
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
    }
  }
}
