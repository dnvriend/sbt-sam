package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.s3

import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{Json, Writes}

object S3EventSource {
  implicit val writes: Writes[S3EventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "S3",
        "Properties" -> Json.obj(
          "Bucket" -> bucketName,
          "Events" -> Json.arr(events.map(_.value))
        )
      )
    )
  })
}
case class S3EventSource(
                        logicalResourceId: String,
                        bucketName: String,
                        events: List[S3Event],
                        ) extends EventSource
