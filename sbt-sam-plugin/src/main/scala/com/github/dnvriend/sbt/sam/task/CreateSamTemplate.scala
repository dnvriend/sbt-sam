package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.{AmazonUser, CredentialsAndRegion, TemplateBody}
import com.github.dnvriend.sbt.sam.state.ProjectState
import play.api.libs.json.{JsObject, JsValue, Json, OWrites}

import scalaz.Monoid
import scalaz._
import scalaz.Scalaz._
import JsonMonoid._

object JsonMonoid {
  implicit val monoid: Monoid[JsObject] = Monoid.instance(_ ++ _, Json.obj())
}

sealed trait Resource
object S3BucketResource {
  implicit val writes: OWrites[S3BucketResource] = OWrites(resource => {
    Json.obj(
      "SbtSamDeploymentBucket" -> Json.obj(
        "Type" -> "AWS::S3::Bucket",
        "Properties" -> ""//resource.properties.foldMap(Json.toJsObject)
      )
    )
  })
}

sealed trait ResourceProperty
object AccessControl {
  val writes: OWrites[AccessControl] = OWrites(prop => Json.obj("AccessControl" -> prop.value))
}
case class AccessControl(value: String) extends ResourceProperty
object BucketName {
  val writes: OWrites[AccessControl] = OWrites(prop => Json.obj("BucketName" -> prop.value))
}
case class BucketName(value: String) extends ResourceProperty
case class S3BucketResource(name: String,
                            properties: List[ResourceProperty]) extends Resource

sealed trait S3BucketPolicy
case object AuthenticatedRead extends S3BucketPolicy
case object AwsExecRead extends S3BucketPolicy
case object BucketOwnerRead extends S3BucketPolicy
case object BucketOwnerFullControl extends S3BucketPolicy
case object LogDeliveryWrite extends S3BucketPolicy
case object Private extends S3BucketPolicy
case object PublicRead extends S3BucketPolicy
case object PublicReadWrite extends S3BucketPolicy

object CreateSamTemplate {
  def run(lambdas: Set[LambdaHandler],
          user: AmazonUser,
          credentialsAndRegion: CredentialsAndRegion,
          stage: String,
          description: String): TemplateBody = {
    TemplateBody(createSamTemplate(createLambdaResources(lambdas, user, credentialsAndRegion), description))
  }

  def fromProjectConfiguration(config: ProjectConfiguration): TemplateBody = {
    TemplateBody(
      (basicTemplate ++
        Json.obj("Resources" -> bucketResource(config.samS3BucketName.value, BucketOwnerFullControl))
        ).toString
    )
  }

  def basicTemplate: JsObject = {
    Json.obj("AWSTemplateFormatVersion" -> "2010-09-09")
  }

  def bucketResource(bucketName: String, bucketPolicy: S3BucketPolicy): JsObject = {
    Json.obj(
      "SbtSamDeploymentBucket" -> Json.obj(
        "Type" -> "AWS::S3::Bucket",
        "Properties" -> Json.obj(
          "AccessControl" -> bucketPolicy.toString,
          "BucketName" -> bucketName
        )
      )
    )
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
