package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._

object CFDynamoDBTableProperties {
  implicit val writes: Writes[CFDynamoDBTableProperties] = Writes.apply(model => {
    import model._
      NonEmptyList(
        Json.toJson(tableName),
        Json.toJson(keySchema),
        Json.toJson(attributeDefinitions),
        Json.toJson(provisionedThroughput),
        Json.toJson(streamSpecification),
        Json.toJson(gsi),
        Json.toJson(tags),
      ).fold(JsMonoids.jsObjectMerge)
  })
}

case class CFDynamoDBTableProperties(
                                      tableName: CFTDynamoDBTableName,
                                      keySchema: CFDynamoDBTableKeySchema,
                                      attributeDefinitions: CFDynamoDBTableAttributeDefinitions,
                                      provisionedThroughput: CFDynamoDBTableProvisionedThroughput,
                                      streamSpecification: Option[CFDynamoDBTableStreamSpecification],
                                      gsi: Option[CFDynamoDBTableGlobalSecondaryIndexes],
                                      tags: CFDynamoDBTableTags
                                    )