package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ Json, Writes }

import scalaz.NonEmptyList
import scalaz.syntax.foldable._

object CloudFormationTemplate {
  implicit val writes: Writes[CloudFormationTemplate] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.toJson(formatVersion),
      Json.toJson(transform),
      Json.toJson(description),
      Json.toJson(metadata),
      Json.toJson(parameters),
      Json.toJson(mappings),
      Json.toJson(conditions),
      Json.toJson(resources),
      Json.toJson(outputs),
    ).fold(JsMonoids.jsObjectMerge)
  })
}

case class CloudFormationTemplate(
    description: Description,
    resources: Resources,
    transform: Option[Transform] = None,
    outputs: Option[Outputs] = None,
    metadata: Option[Metadata] = None,
    parameters: Option[Parameters] = None,
    mappings: Option[Mappings] = None,
    conditions: Option[Conditions] = None,
    formatVersion: AWSTemplateFormatVersion = AWSTemplateFormatVersion()
)
