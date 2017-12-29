package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsValue, Json, Writes }

import scalaz.NonEmptyList
import scalaz.Scalaz._

object CFDynamoDBTable {
  val writes: Writes[CFDynamoDBTable] = Writes.apply(model => {
    import model._
    val resourceType: JsValue = Json.obj("Type" -> "AWS::DynamoDB::Table")
    Json.obj(
      logicalName -> NonEmptyList(resourceType, Json.toJson(tableProperties)).foldMap(identity)(JsMonoids.jsObjectMerge)
    )
  })
}

case class CFDynamoDBTable(logicalName: String, tableProperties: CFDynamoDBTableProperties) extends Resource
