package com.github.dnvriend.sbt.sam.cf.resource.cognito.userpool

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.task.ProjectConfiguration
import play.api.libs.json.{JsObject, JsValue, Json}

object UserPool {
  def logicalResourceId(config: ProjectConfiguration): String = {
    "ServerlessUserPool"
  }

  /**
    * Returns a CloudFormation configuration based on the ProjectConfiguration
    */
  def resource(config: ProjectConfiguration): JsObject = {
    Json.obj(
      logicalResourceId(config) -> (Json.obj(
        "Type" -> "AWS::Cognito::UserPool"
      ) ++ CloudFormation.properties(
        propUserPoolName(config),
        propAdminCreateUserConfig(config),
        propPolicies(config),
      ))
    )
  }

  /**
    * A string used to name the user pool.
    */
  def propUserPoolName(config: ProjectConfiguration): JsValue = {
    Json.obj("UserPoolName" -> "auth_pool")
  }

  /**
    * The type of configuration for creating a new user profile.
    */
  def propAdminCreateUserConfig(config: ProjectConfiguration): JsValue = {
    Json.obj("AdminCreateUserConfig" -> Json.obj(
      "AllowAdminCreateUserOnly" -> true,
      "UnusedAccountValidityDays" -> 30
    ))
  }

  /**
    * The policies associated with the Amazon Cognito user pool.
    */
  def propPolicies(config: ProjectConfiguration): JsValue = {
    Json.obj(
      "Policies" -> Json.obj(
        "PasswordPolicy" -> Json.obj(
          "MinimumLength" -> 6,
          "RequireLowercase" -> true,
          "RequireNumbers" -> false,
          "RequireSymbols" -> false,
          "RequireUppercase" -> false
        )
      )
    )
  }

  /**
    * When the logical ID of this resource is provided to the Ref intrinsic function, Ref returns a generated ID,
    * such as 'us-east-2_zgaEXAMPLE'
    */
  def logicalId(config: ProjectConfiguration): JsValue = {
    CloudFormation.ref(logicalResourceId(config))
  }

  /**
    * The provider name of the Amazon Cognito user pool, specified as a String.
    */
  def providerName(config: ProjectConfiguration): JsValue = {
    CloudFormation.getAtt(logicalResourceId(config), "ProviderName")
  }

  /**
    * The URL of the provider of the Amazon Cognito user pool, specified as a String.
    */
  def providerUrl(config: ProjectConfiguration): JsValue = {
    CloudFormation.getAtt(logicalResourceId(config), "ProviderURL")
  }

  /**
    * The Amazon Resource Name (ARN) of the user pool, such as
    * 'arn:aws:cognito-idp:us-east-2:123412341234:userpool/us-east-1 _123412341'
    */
  def arn(config: ProjectConfiguration): JsValue = {
    CloudFormation.getAtt(logicalResourceId(config), "Arn")
  }
}