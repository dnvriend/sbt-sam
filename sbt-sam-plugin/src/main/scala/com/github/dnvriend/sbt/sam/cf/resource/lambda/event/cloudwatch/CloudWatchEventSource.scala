package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.cloudwatch

import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{ Writes, Json }

object CloudWatchEventSource {
  implicit val writes: Writes[CloudWatchEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceName -> Json.obj(
        "Type" -> "CloudWatchEvent",
        "Properties" -> Json.obj(
          "Pattern" -> Json.parse(pattern)
        )
      )
    )
  })
}

case class CloudWatchEventSource(
    logicalResourceName: String,
    pattern: String
) extends EventSource
