package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.s3

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._

object S3EventSource {
  implicit val writes: Writes[S3EventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "S3",
        "Properties" -> NonEmptyList(
          Json.toJson(determineName(importName, bucketName)),
          Json.obj("Events" -> Json.arr(events.map(_.value)))
        ).foldMap(identity)(JsMonoids.jsObjectMerge)
      )
    )
  })

  def determineName(importName: Option[String], resourceName: Option[String]): Option[JsValue] = {
    importName.map(name => bucketNameFromImport(name))
      .orElse(resourceName.map(name => bucketNameToArn(name)))
  }

  def bucketNameFromImport(importName: String): JsValue = {
    Json.obj("Bucket" -> CloudFormation.importValue(importName))
  }

  def bucketNameToArn(bucketName: String): JsValue = {
    Json.obj("Bucket" -> CloudFormation.bucketArn(bucketName))
  }
}
case class S3EventSource(
                          logicalName: String,
                          bucketName: Option[String],
                          importName: Option[String],
                          events: List[S3Event],
                        ) extends EventSource
