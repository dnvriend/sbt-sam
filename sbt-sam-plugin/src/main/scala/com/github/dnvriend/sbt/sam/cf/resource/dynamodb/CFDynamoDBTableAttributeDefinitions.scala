package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json._

object CFDynamoDBTableAttributeDefinition {
  implicit val writes: Writes[CFDynamoDBTableAttributeDefinition] = Writes.apply(model => {
    import model._
    Json.obj(
      "AttributeName" -> attributeName,
      "AttributeType" -> attributeType
    )
  })
}

case class CFDynamoDBTableAttributeDefinition(attributeName: String, attributeType: String)

object CFDynamoDBTableAttributeDefinitions {
  implicit val writes: Writes[CFDynamoDBTableAttributeDefinitions] = Writes.apply(model => {
    import model._
    Json.obj("AttributeDefinitions" -> attributes.map(Json.toJson(_)))
  })
}

case class CFDynamoDBTableAttributeDefinitions(attributes: List[CFDynamoDBTableAttributeDefinition])