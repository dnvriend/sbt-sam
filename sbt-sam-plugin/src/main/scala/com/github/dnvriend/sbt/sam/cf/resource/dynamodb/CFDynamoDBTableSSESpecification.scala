package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json.{ Json, Writes }

object CFDynamoDBTableSSESpecification {
  implicit val writes: Writes[CFDynamoDBTableSSESpecification] = Writes.apply(model => {
    import model._
    Json.obj("SSESpecification" -> Json.obj(
      "SSEEnabled" -> true
    ))
  })
}

/**
 * DynamoDB SSESpecification, The SSESpecification property is part of the AWS::DynamoDB::Table resource
 * that specifies the settings to enable server-side encryption.
 */
case class CFDynamoDBTableSSESpecification()