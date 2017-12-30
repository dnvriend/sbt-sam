package com.github.dnvriend.sbt.sam.cf.resource.firehose.s3

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz._
import scalaz.Scalaz._

object CFS3FirehoseProperties {
  implicit val writes: Writes[CFS3FirehoseProperties] = Writes.apply(model => {
    import model._
    val config: JsValue = List(
      Json.obj("BucketARN" -> ""),
      Json.obj("BufferingHints" -> Json.obj(
        "IntervalInSeconds" -> bufferingIntervalInSeconds,
        "SizeInMBs" -> bufferingSize
      )),
    ).widen[JsValue].foldMap(identity)(JsMonoids.jsObjectMerge)

    Json.obj("Properties" -> Json.obj(
      "ExtendedS3DestinationConfiguration" -> config
    ))
  })
}

case class CFS3FirehoseProperties(
                                   name: String,
                                   bucketName: String,
                                   roleArn: String,
                                   kinesisStreamSource: Option[String] = None,
                                   compression: Option[String] = None,
                                   encryptionKey: Option[String] = None,
                                   bufferingIntervalInSeconds: Int = 300,
                                   bufferingSize: Int = 5,
                                 )

object CFS3Firehose {
  implicit val writes: Writes[CFS3Firehose] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.obj("Type" -> "AWS::KinesisFirehose::DeliveryStream"),
      Json.toJson(properties)
    ).foldMap(identity)(JsMonoids.jsObjectMerge)
  })
}
case class CFS3Firehose(properties: CFS3FirehoseProperties) extends Resource
