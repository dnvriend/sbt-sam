package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.NonEmptyList
import scalaz.Scalaz._

object CFDynamoDBTableGlobalSecondaryIndex {
  implicit val writes: Writes[CFDynamoDBTableGlobalSecondaryIndex] = Writes.apply(model => {
    import model._
    val index: JsValue = {
      Json.obj(
        "IndexName" -> indexName,
        "Projection" -> Json.obj(
          "ProjectionType" -> projectionType
        )
      )
    }
    NonEmptyList(
      index,
      Json.toJson(keySchema),
      Json.toJson(projectionType),
      Json.toJson(provisionedThroughput),
    ).foldMap(identity)(JsMonoids.jsObjectMerge)
  })
}
case class CFDynamoDBTableGlobalSecondaryIndex(indexName: String,
                                               keySchema: CFDynamoDBTableKeySchema,
                                               projectionType: String,
                                               provisionedThroughput: CFDynamoDBTableProvisionedThroughput
                                              )