package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json.{ Json, Writes }

object CFDynamoDBTableStreamSpecification {
  implicit val writes: Writes[CFDynamoDBTableStreamSpecification] = Writes.apply(model => {
    Json.obj(
      "StreamSpecification" -> Json.obj(
        "StreamViewType" -> model.streamViewType
      )
    )
  })
}

case class CFDynamoDBTableStreamSpecification(streamViewType: String)
