package com.github.dnvriend.sbt.sam.cf.resource.lambda.event

import play.api.libs.json.{ JsNull, Writes }

object NoEventSource {
  implicit val writes: Writes[NoEventSource] = Writes.apply(_ => JsNull)
}

case class NoEventSource() extends EventSource
