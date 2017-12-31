package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{ Json, Writes }

object CFDynamoDBTable {
  val writes: Writes[CFDynamoDBTable] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::DynamoDB::Table",
        "Properties" -> Json.toJson(tableProperties)
      )
    )
  })
}

case class CFDynamoDBTable(logicalName: String, tableProperties: CFDynamoDBTableProperties) extends Resource
