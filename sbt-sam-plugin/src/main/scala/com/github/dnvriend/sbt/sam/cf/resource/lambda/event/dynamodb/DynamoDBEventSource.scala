package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.dynamodb

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{Json, Writes}

object DynamoDBEventSource {
  implicit val writes: Writes[DynamoDBEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "DynamoDB",
        "Properties" -> Json.obj(
          "Stream" -> CloudFormation.getAtt(logicalResourceIdDynamoDBTableName, "StreamArn"),
          "BatchSize" -> batchSize,
          "StartingPosition" -> startingPosition,
        )
      ))
  })
}
case class DynamoDBEventSource(
                              logicalResourceId: String,
                              logicalResourceIdDynamoDBTableName: String,
                              batchSize: Int,
                              startingPosition: String,
                              ) extends EventSource

