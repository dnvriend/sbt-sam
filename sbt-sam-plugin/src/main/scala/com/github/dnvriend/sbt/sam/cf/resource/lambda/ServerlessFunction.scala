package com.github.dnvriend.sbt.sam.cf.resource.lambda

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{ Json, Writes }

object ServerlessFunction {
  implicit val writes: Writes[ServerlessFunction] = Writes.apply(model => {
    import model._
    Json.obj(
      lambdaName -> Json.obj(
        "Type" -> "AWS::Serverless::Function",
        "Properties" -> Json.obj(
          "Handler" -> s"$fqcn::handleRequest",
          "Runtime" -> "java8",
          "CodeUri" -> Json.obj(
            "Bucket" -> deploymentBucketName,
            "Key" -> jarName,
            "Version" -> latestVersion
          ),
          "Policies" -> Json.arr(
            "AmazonDynamoDBFullAccess",
            "CloudWatchFullAccess",
            "CloudWatchLogsFullAccess",
            "AmazonSNSFullAccess",
            "AmazonKinesisFullAccess",
            "AWSKeyManagementServicePowerUser",
            "AmazonKinesisFirehoseFullAccess",
          ),
          "Description" -> description,
          "MemorySize" -> memorySize,
          "Timeout" -> timeout,
          "Tracing" -> "Active",
          "Environment" -> Json.obj(
            "Variables" -> Json.obj(
              "STAGE" -> stage,
              "PROJECT_NAME" -> projectName,
              "VERSION" -> projectVersion,
              "AWS_ACCOUNT_ID" -> CloudFormation.accountId
            )
          ),
          "Tags" -> Json.obj(
            "sbt:sam:projectName" -> projectName,
            "sbt:sam:projectVersion" -> projectVersion,
            "sbt:sam:stage" -> stage
          ),
          "Events" -> Json.toJson(eventSource)
        )
      )
    )
  })
}

case class ServerlessFunction(
    lambdaName: String,
    fqcn: String,
    projectName: String,
    projectVersion: String,
    stage: String,
    deploymentBucketName: String,
    jarName: String,
    latestVersion: String,
    description: String,
    memorySize: Int,
    timeout: Int,
    eventSource: EventSource
) extends Resource