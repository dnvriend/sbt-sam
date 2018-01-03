package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.kinesis

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._

object KinesisEventSource {
  implicit val writes: Writes[KinesisEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "Kinesis",
        "Properties" -> NonEmptyList(
          Json.obj("StartingPosition" -> startingPosition),
          Json.obj("BatchSize" -> batchSize),
          Json.toJson(determineName(importName, streamName))
        ).foldMap(identity)(JsMonoids.jsObjectMerge)
      )
    )
  })

  def determineName(importName: Option[String], resourceName: Option[String]): Option[JsValue] = {
    importName.map(name => streamNameFromImport(name))
      .orElse(resourceName.map(name => streamNameToArn(name)))
  }

  def streamNameFromImport(importName: String): JsValue = {
    Json.obj("Stream" -> CloudFormation.importValue(importName))
  }

  def streamNameToArn(streamName: String): JsValue = {
    Json.obj("Stream" -> CloudFormation.kinesisArn(streamName))
  }
}

case class KinesisEventSource(
                               logicalName: String,
                               streamName: Option[String],
                               importName: Option[String],
                               batchSize: Int,
                               startingPosition: String,
                             ) extends EventSource
