package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.TemplateBody
import play.api.libs.json.{ JsObject, Json }

import scalaz.Monoid

object CloudFormationTemplates {
  implicit val monoid: Monoid[JsObject] = Monoid.instance(_ ++ _, Json.obj())
  val templateFormatVersion: JsObject = Json.obj("AWSTemplateFormatVersion" -> "2010-09-09")
  val samTransform: JsObject = Json.obj("Transform" -> "AWS::Serverless-2016-10-31")

  /**
   * Returns the basic cloud formation template to create the stack and deployment bucket
   */
  def deploymentBucketTemplate(config: ProjectConfiguration): TemplateBody = {
    TemplateBody.fromJson(
      templateFormatVersion ++
        resources(bucketResource("SbtSamDeploymentBucket", config.samS3BucketName.value))
    )
  }

  def updateTemplate(config: ProjectConfiguration): TemplateBody = {
    TemplateBody.fromJson(
      templateFormatVersion ++
        samTransform ++
        resources(bucketResource("SbtSamDeploymentBucket", config.samS3BucketName.value))
    )
  }

  /**
   * Merges a sequence of JsObjects into one,
   */
  def resources(resources: JsObject*): JsObject =
    Json.obj("Resources" -> resources.reduce(_ ++ _))

  def bucketResource(resourceName: String, bucketName: String): JsObject = {
    Json.obj(
      resourceName -> Json.obj(
        "Type" -> "AWS::S3::Bucket",
        "Properties" -> Json.obj(
          "AccessControl" -> "BucketOwnerFullControl",
          "BucketName" -> bucketName
        )
      )
    )
  }

  def functionResource(
    resourceName: String,
    fqcn: String,
    codeUri: String,
    functionName: String,
    description: String,
    memorySize: Int,
    timeout: Int,
    role: String,
    policies: List[String],
    event: JsObject): JsObject = {
    Json.obj(
      resourceName -> Json.obj(
        "Type" -> "AWS::Serverless::Function",
        "Properties" -> Json.obj(
          "Handler" -> s"$fqcn::handleRequest",
          "Runtime" -> "java8",
          "CodeUri" -> codeUri, // s3://bucketName/codepackage.zip
          "FunctionName" -> functionName,
          "Description" -> description,
          "MemorySize" -> memorySize,
          "Timeout" -> timeout,
          "Role" -> role,
          "Policies" -> Json.arr("AWSLambdaDynamoDBExecutionRole"),
          "Tracing" -> functionName,
          "Events" -> event
        )
      )
    )
  }

  def apiGatewayEvent(eventName: String, path: String = "/", method: String = "get"): JsObject = {
    Json.obj(
      eventName -> Json.obj(
        "Type" -> "Api",
        "Properties" -> Json.obj(
          "Path" -> path,
          "Method" -> method
        )
      ))
  }

  def dynamoDbStreamEvent(eventName: String): JsObject = {
    Json.obj(
      eventName -> Json.obj(
        "Type" -> "DynamoDB",
        "Properties" -> Json.obj(
          "Stream" -> "", // arn here
          "BatchSize" -> 100,
          "StartingPosition" -> "TRIM_HORIZON"
        )
      ))
  }
}
