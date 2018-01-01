package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json.{ Json, Writes }

object CFDynamoDBTableGlobalSecondaryIndexes {
  implicit val writes: Writes[CFDynamoDBTableGlobalSecondaryIndexes] = Writes.apply(model => {
    Json.obj(
      "GlobalSecondaryIndexes" -> model.gsis.map(Json.toJson(_))
    )
  })
}
case class CFDynamoDBTableGlobalSecondaryIndexes(gsis: List[CFDynamoDBTableGlobalSecondaryIndex])