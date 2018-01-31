package com.github.dnvriend.sbt.sam.cf.resource.lambda

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{ JsObject, JsValue, Json, Writes }

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
          "Policies" -> managedPolicies,
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
        ).++(determineVpcConfig)
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
    managedPolicies: List[String],
    eventSource: EventSource,
    vpcConfig: Option[VpcConfig]
) extends Resource {

  val determineVpcConfig: JsObject = {
    vpcConfig.fold(JsObject(Nil))(config => Json.obj(
      "VpcConfig" -> Json.obj(
        "SecurityGroupIds" -> config.securityGroupIds,
        "SubnetIds" -> config.subnetIds
      )
    ))

  }
}

case class VpcConfig(
    securityGroupIds: List[String],
    subnetIds: List[String]
)