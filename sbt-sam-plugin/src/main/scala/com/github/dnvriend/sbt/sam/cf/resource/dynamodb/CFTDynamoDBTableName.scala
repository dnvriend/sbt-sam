package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json.{ Json, Writes }

object CFTDynamoDBTableName {
  implicit val writes: Writes[CFTDynamoDBTableName] = Writes.apply(model => {
    Json.obj("TableName" -> model.tableName)
  })
}

case class CFTDynamoDBTableName(tableName: String)
