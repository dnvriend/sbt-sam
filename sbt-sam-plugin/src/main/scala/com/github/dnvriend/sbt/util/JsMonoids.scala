package com.github.dnvriend.sbt.util

import play.api.libs.json.{ JsNull, JsObject, JsValue }

import scalaz.Monoid

object JsMonoids {
  val jsObjectMerge: Monoid[JsValue] = Monoid.instance({
    case (l, JsNull)                => l
    case (JsNull, r)                => r
    case (l: JsObject, r: JsObject) => l ++ r
    case (l, _)                     => l
  }, JsNull)
}
