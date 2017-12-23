package com.github.dnvriend.lambda

import java.io.InputStream

import play.api.libs.json.{ Format, Json }

object ScheduledEvent {
  implicit val format: Format[ScheduledEvent] = Json.format
  def parse(is: InputStream): ScheduledEvent = {
    Json.parse(is).as[ScheduledEvent]
  }
}
case class ScheduledEvent(
    account: String,
    region: String,
    `detail-type`: String,
    source: String,
    time: String,
    id: String,
    resources: List[String]
)