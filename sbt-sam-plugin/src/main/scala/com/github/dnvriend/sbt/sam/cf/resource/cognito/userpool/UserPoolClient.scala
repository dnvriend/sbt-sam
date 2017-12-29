package com.github.dnvriend.sbt.sam.cf.resource.cognito.userpool

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.task.ProjectConfiguration
import play.api.libs.json.{JsObject, Json}

/**
  * creates an Amazon Cognito user pool client.
  */
object UserPoolClient {
  def logicalResourceId(config: ProjectConfiguration): String = {
    "ServerlessUserPoolClient"
  }

  def resource(config: ProjectConfiguration): JsObject = {
    Json.obj(
      logicalResourceId(config) -> (Json.obj(
        "Type" -> "AWS::Cognito::UserPoolClient",
        "DependsOn" -> UserPool.logicalResourceId(config),
      ) ++ CloudFormation.properties(
        propClientName(config),
        propExplicitAuthFlows(config),
        propUserPoolId(config),
      ))
    )
  }

  /**
    * The client name for the user pool client that you want to create.
    */
  def propClientName(config: ProjectConfiguration): JsObject = {
    val clientName = "client" //todo: set the client name
    Json.obj("ClientName" -> clientName)
  }

  /**
    * The explicit authentication flows, which can be one of the following:
    * - ADMIN_NO_SRP_AUTH
    * - CUSTOM_AUTH_FLOW_ONLY.
    */
  def propExplicitAuthFlows(config: ProjectConfiguration): JsObject = {
    Json.obj("ExplicitAuthFlows" -> Json.arr("ADMIN_NO_SRP_AUTH"))
  }

  /**
    * The user pool ID for the user pool where you want to create a client.
    */
  def propUserPoolId(config: ProjectConfiguration): JsObject = {
    Json.obj("UserPoolId" -> UserPool.logicalId(config))
  }
}