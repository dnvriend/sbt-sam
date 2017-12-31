package com.github.dnvriend.sbt.sam.cf.resource.cognito.userpool

import play.api.libs.json.{ Json, Writes }

object UserPoolUser {
  implicit val writes: Writes[UserPoolUser] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::Cognito::UserPoolUser",
        "DependsOn" -> UserPool.LogicalName,
        "Properties" -> Json.obj(
          "Username" -> username,
          "UserPoolId" -> UserPool.logicalId,
          "DesiredDeliveryMediums" -> "EMAIL",
          "ForceAliasCreation" -> false,
          "UserAttributes" -> List.empty[String]
        )
      )
    )
  })
}

case class UserPoolUser(
    username: String,
    logicalName: String
)