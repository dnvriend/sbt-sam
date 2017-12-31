package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsValue, Json, Writes }

import scalaz.syntax.foldable._
import scalaz.std.AllInstances._

object Metadata {
  implicit val writes: Writes[Metadata] = Writes.apply(model => {
    import model._
    Json.obj("Metadata" -> arbitrary.toList.foldMap(identity)(JsMonoids.jsObjectMerge))
  }
  )
}

/**
 * You can use the optional Metadata section to include arbitrary JSON objects that provide details about the template.
 * For example, you can include template implementation details about specific resources.
 */
case class Metadata(arbitrary: JsValue*)
