package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.sns

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{ Json, Writes }

object SnsEventSource {
  implicit val writes: Writes[SnsEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "SNS",
        "Properties" -> Json.obj(
          "Topic" -> CloudFormation.snsArn(topicName)
        )
      )
    )
  })
}
case class SnsEventSource(logicalResourceId: String, topicName: String) extends EventSource
