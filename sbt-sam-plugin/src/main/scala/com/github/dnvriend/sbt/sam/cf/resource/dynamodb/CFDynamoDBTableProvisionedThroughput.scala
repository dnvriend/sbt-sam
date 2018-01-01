package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import play.api.libs.json.{ Json, Writes }

object CFDynamoDBTableProvisionedThroughput {
  implicit val writes: Writes[CFDynamoDBTableProvisionedThroughput] = Writes.apply(model => {
    import model._
    Json.obj(
      "ProvisionedThroughput" -> Json.obj(
        "ReadCapacityUnits" -> rcu,
        "WriteCapacityUnits" -> wcu
      )
    )
  })
}

case class CFDynamoDBTableProvisionedThroughput(rcu: Int, wcu: Int)