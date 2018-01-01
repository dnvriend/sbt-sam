package com.github.dnvriend.sbt.sam.cf.resource.cognito.userpool

import play.api.libs.json.{Json, Writes}
import com.github.dnvriend.sbt.sam.cf.resource.Resource

/**
  * creates an Amazon Cognito user pool client.
  */
object UserPoolClient {
  final val LogicalName: String = "ServerlessUserPoolClient"

  implicit val writes: Writes[UserPoolClient] = Writes.apply(model => {
    import model._
    Json.obj(
      LogicalName -> Json.obj(
        "Type" -> "AWS::Cognito::UserPoolClient",
        "DependsOn" -> UserPool.LogicalName,
        "Properties" -> Json.obj(
          "ClientName" -> clientName,
          "ExplicitAuthFlows" -> Json.arr("ADMIN_NO_SRP_AUTH"),
          "UserPoolId" -> UserPool.logicalId,
        )
      )
    )
  })
}

case class UserPoolClient(clientName: String) extends Resource