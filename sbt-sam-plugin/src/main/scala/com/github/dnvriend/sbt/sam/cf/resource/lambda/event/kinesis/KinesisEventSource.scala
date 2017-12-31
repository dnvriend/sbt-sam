package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.kinesis

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{Json, Writes}

object KinesisEventSource {
  implicit val writes: Writes[KinesisEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "Kinesis",
        "Properties" -> Json.obj(
          "Stream" -> CloudFormation.kinesisArn(streamName),
          "BatchSize" -> batchSize,
          "StartingPosition" -> startingPosition,
        )
      )
    )
  })
}
case class KinesisEventSource(
                             logicalResourceId: String,
                             streamName: String,
                             batchSize: Int,
                             startingPosition: String,
                             ) extends EventSource
