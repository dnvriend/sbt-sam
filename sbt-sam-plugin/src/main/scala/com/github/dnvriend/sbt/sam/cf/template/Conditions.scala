package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsValue, Json, Writes }

import scalaz.syntax.foldable._
import scalaz.std.AllInstances._

object Condition {
  implicit val writes: Writes[Condition] = Writes.apply(model => {
    import model._
    Json.obj(logicalId -> intrinsicFunction)
  })
}

/**
 * Condition control whether certain resources are created or whether certain resource properties are assigned a value
 * during stack creation or update
 */
final case class Condition(logicalId: String, intrinsicFunction: JsValue)

object Conditions {
  implicit val writes: Writes[Conditions] = Writes.apply(model => {
    import model._
    Json.obj("Conditions" -> conditions.foldMap(Json.toJson(_))(JsMonoids.jsObjectMerge))
  })
}

/**
 * The optional Conditions section includes statements that define when a resource is created or when a property is defined.
 * For example, you can compare whether a value is equal to another value. Based on the result of that condition, you can
 * conditionally create resources. If you have multiple conditions, separate them with commas.
 */
final case class Conditions(conditions: List[Condition])
