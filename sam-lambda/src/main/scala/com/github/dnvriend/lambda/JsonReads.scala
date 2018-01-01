package com.github.dnvriend.lambda

import play.api.libs.json.{ JsError, Reads }

object JsonReads {
  implicit val nothingReads: Reads[Nothing] = {
    Reads.apply[Nothing](_ => JsError())
  }
}
