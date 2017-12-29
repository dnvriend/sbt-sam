package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsValue, Json, Writes }

import scalaz.syntax.foldable._
import scalaz.std.AllInstances._

object Parameters {
  implicit val writes: Writes[Parameters] = Writes.apply(model => {
    import model._
    Json.obj("Parameters" -> arbitrary.toList.foldMap(identity)(JsMonoids.jsObjectMerge))
  }
  )
}

/**
 * Use the optional Parameters section to customize your templates. Parameters enable you to input custom
 * values to your template each time you create or update a stack.
 */
case class Parameters(arbitrary: JsValue*)
