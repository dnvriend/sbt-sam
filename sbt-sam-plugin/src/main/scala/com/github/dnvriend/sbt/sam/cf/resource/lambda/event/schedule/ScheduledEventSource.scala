package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.schedule

import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{ Json, Writes }

object ScheduledEventSource {
  implicit val writes: Writes[ScheduledEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "Schedule",
        "Properties" -> Json.obj(
          "Schedule" -> schedule
        )
      )
    )
  })
}
case class ScheduledEventSource(logicalResourceId: String, schedule: String) extends EventSource
