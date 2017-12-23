package com.github.dnvriend.lambda

import java.io.InputStream

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object Kinesis {
  implicit val reads: Reads[Kinesis] = Json.reads
}
case class Kinesis(
    approximateArrivalTimestamp: Long,
    partitionKey: String,
    data: String,
    kinesisSchemaVersion: String,
    sequenceNumber: String
)

object KinesisEvent {
  implicit val reads: Reads[KinesisEvent] = Json.reads[KinesisEvent]
  def parse(input: InputStream): List[KinesisEvent] = {
    (Json.parse(input) \ "Records").as[List[KinesisEvent]]
  }
}
case class KinesisEvent(
    eventID: String,
    eventVersion: String,
    kinesis: Kinesis,
    invokeIdentityArn: String,
    eventName: String,
    eventSourceARN: String,
    eventSource: String,
    awsRegion: String
) {
  def dataAsBytes: Array[Byte] = java.util.Base64.getDecoder.decode(dataAsBase64)
  def dataAsBase64: String = kinesis.data
}