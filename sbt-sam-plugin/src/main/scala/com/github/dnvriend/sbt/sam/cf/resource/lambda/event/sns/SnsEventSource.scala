package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.sns

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._

object SnsEventSource {
  implicit val writes: Writes[SnsEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "SNS",
        "Properties" -> NonEmptyList(
          Json.toJson(determineName(importName, topicName))
        ).foldMap(identity)(JsMonoids.jsObjectMerge)
      )
    )
  })

  def determineName(importName: Option[String], resourceName: Option[String]): Option[JsValue] = {
    importName.map(name => topicNameFromImport(name))
      .orElse(resourceName.map(name => topicNameToArn(name)))
  }

  def topicNameFromImport(importName: String): JsValue = {
    Json.obj("Topic" -> CloudFormation.importValue(importName))
  }

  def topicNameToArn(topicName: String): JsValue = {
    Json.obj("Topic" -> CloudFormation.snsArn(topicName))
  }

}
case class SnsEventSource(
                           logicalName: String,
                           topicName: Option[String],
                           importName: Option[String],
                         ) extends EventSource
