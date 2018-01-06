package com.github.dnvriend.sbt.sam.cf.resource.cognito.userpool

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{JsValue, Json, Writes}

object UserPool {
  final val LogicalName: String = "ServerlessUserPool"

  implicit val writes: Writes[UserPool] = Writes.apply(model => {
    import model._
    Json.obj(
      LogicalName -> Json.obj(
        "Type" -> "AWS::Cognito::UserPool",
        "Properties" -> Json.obj(
          "UserPoolName" -> name,
          "AdminCreateUserConfig" -> Json.obj(
            "AllowAdminCreateUserOnly" -> true,
            "UnusedAccountValidityDays" -> 30
          ),
          "Policies" -> Json.obj(
            "PasswordPolicy" -> Json.obj(
              "MinimumLength" -> minimumLength,
              "RequireLowercase" -> requireLowercase,
              "RequireNumbers" -> requireNumbers,
              "RequireSymbols" -> requireSymbols,
              "RequireUppercase" -> requireUppercase
            )
          )
        )
      )
    )
  })

  /**
    * When the logical ID of this resource is provided to the Ref intrinsic function, Ref returns a generated ID,
    * such as 'us-east-2_zgaEXAMPLE'
    */
  def logicalId: JsValue = {
    CloudFormation.ref(LogicalName)
  }

  /**
    * The provider name of the Amazon Cognito user pool, specified as a String.
    */
  def providerName: JsValue = {
    CloudFormation.getAtt(LogicalName, "ProviderName")
  }

  /**
    * The URL of the provider of the Amazon Cognito user pool, specified as a String.
    */
  def providerUrl: JsValue = {
    CloudFormation.getAtt(LogicalName, "ProviderURL")
  }

  /**
    * The Amazon Resource Name (ARN) of the user pool, such as
    * 'arn:aws:cognito-idp:us-east-2:123412341234:userpool/us-east-1 _123412341'
    */
  def arn: JsValue = {
    CloudFormation.getAtt(LogicalName, "Arn")
  }

  def determineProviderARN(importName: Option[String]): JsValue = {
    importName.map(name => CloudFormation.importValue(name))
        .getOrElse(arn)
  }
}

case class UserPool(name: String,
                    minimumLength: Int,
                    requireLowercase: Boolean,
                    requireNumbers: Boolean,
                    requireSymbols: Boolean,
                    requireUppercase: Boolean,
                   ) extends Resource
