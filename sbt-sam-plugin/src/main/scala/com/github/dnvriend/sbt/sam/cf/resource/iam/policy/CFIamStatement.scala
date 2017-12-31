package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsValue, Json, Writes }

import scalaz.syntax.all._
import scalaz.NonEmptyList

object CFIamStatement {
  implicit val writes: Writes[CFIamStatement] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.obj("Effect" -> effect),
      Json.obj("Action" -> actions),
      Json.toJson(principal),
      Json.toJson(resource.map(value => Json.obj("Resource" -> value)))
    ).widen[JsValue].foldMap(identity)(JsMonoids.jsObjectMerge)
  })

  def allowAssumeRole(principal: CFPrincipal): CFIamStatement = {
    CFIamStatement("Allow", List("sts:AssumeRole"), Option(principal), None)
  }
}
case class CFIamStatement(
    effect: String,
    actions: List[String],
    principal: Option[CFPrincipal],
    resource: Option[String])
