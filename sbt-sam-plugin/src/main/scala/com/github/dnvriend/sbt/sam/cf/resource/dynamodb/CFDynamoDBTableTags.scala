package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import play.api.libs.json.{ Json, Writes }

object CFDynamoDBTableTags {
  implicit val writes: Writes[CFDynamoDBTableTags] = Writes.apply(model => {
    import model._
    Json.obj(
      "Tags" -> tags.map(Json.toJson(_))
    )
  })
}

case class CFDynamoDBTableTags(tags: List[ResourceTag])

