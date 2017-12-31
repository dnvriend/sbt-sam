package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json.{ JsValue, Json, Writes }

object CFDynamoDbTableHashKey {
  implicit val writes: Writes[CFDynamoDbTableHashKey] = Writes.apply(model => {
    import model._
    Json.obj(
      "AttributeName" -> attributeName,
      "KeyType" -> "HASH"
    )
  })
}

case class CFDynamoDbTableHashKey(attributeName: String)

object CFDynamoDbTableRangeKey {
  implicit val writes: Writes[CFDynamoDbTableRangeKey] = Writes.apply(model => {
    import model._
    Json.obj(
      "AttributeName" -> attributeName,
      "KeyType" -> "RANGE"
    )
  })
}

case class CFDynamoDbTableRangeKey(attributeName: String)

object CFDynamoDBTableKeySchema {
  implicit val writes: Writes[CFDynamoDBTableKeySchema] = Writes.apply(model => {
    import model._
    val keySchema: List[JsValue] = List(Json.toJson(hashKey)) ++ rangeKey.map(key => Json.toJson(key))
    Json.obj("KeySchema" -> keySchema)
  })
}
case class CFDynamoDBTableKeySchema(hashKey: CFDynamoDbTableHashKey, rangeKey: Option[CFDynamoDbTableRangeKey])