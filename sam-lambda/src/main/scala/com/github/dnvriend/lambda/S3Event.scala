package com.github.dnvriend.lambda

import play.api.libs.json._

import scala.tools.nsc.interpreter.InputStream

object S3Event {
  implicit val reads: Reads[S3Event] = Json.reads

  def parse(input: InputStream): List[S3Event] = {

    (Json.parse(input) \ "Records").as[List[S3Event]]
  }
}
case class S3Event(
                    eventName: S3EventName,
                    eventSource: String,
                    awsRegion: String,
                    s3: S3,
                  )

object Bucket {
  implicit val reads: Reads[Bucket] = Json.reads
}
case class Bucket(
                   arn: String,
                   name: String,
                 )

object S3{
  implicit val reads: Reads[S3] = Json.reads
}
case class S3(
               `object`: Object,
               bucket: Bucket
             )

object Object {
  implicit val reads: Reads[Object] = Json.reads
}
case class Object(
                   key: String,
                   size: Option[Int],
                 )

object S3EventName {
  implicit val reads: Reads[S3EventName] = new Reads[S3EventName] {
    override def reads(json: JsValue): JsResult[S3EventName] = json.validate[String].map(toEventObj)
  }

  case object ObjectCreatedAll extends S3EventName { val value = "ObjectCreated:*" }
  case object ObjectCreatedPut extends S3EventName { val value = "ObjectCreated:Put" }
  case object ObjectCreatedPost extends S3EventName { val value = "ObjectCreated:Post" }
  case object ObjectCreatedCopy extends S3EventName { val value = "ObjectCreated:Copy" }
  case object ObjectCreatedCompleteMultipartUpload extends S3EventName { val value = "ObjectCreated:CompleteMultipartUpload" }
  case object ObjectRemovedAll extends S3EventName { val value = "ObjectRemoved:*" }
  case object ObjectRemovedDelete extends S3EventName { val value = "ObjectRemoved:Delete" }
  case object ObjectRemovedDeleteMarkerCreated extends S3EventName { val value = "ObjectRemoved:DeleteMarkerCreated" }
  case object ReducedRedundancyLostObject extends S3EventName { val value = "ReducedRedundancyLostObject" }

  def toEventObj(event: String): S3EventName = event match {
    case "ObjectCreated:*"                       => ObjectCreatedAll
    case "ObjectCreated:Put"                     => ObjectCreatedPut
    case "ObjectCreated:Post"                    => ObjectCreatedPost
    case "ObjectCreated:Copy"                    => ObjectCreatedCopy
    case "ObjectCreated:CompleteMultipartUpload" => ObjectCreatedCompleteMultipartUpload
    case "ObjectRemoved:*"                       => ObjectRemovedAll
    case "ObjectRemoved:Delete"                  => ObjectRemovedDelete
    case "ObjectRemoved:DeleteMarkerCreated"     => ObjectRemovedDeleteMarkerCreated
    case "ReducedRedundancyLostObject"           => ReducedRedundancyLostObject
    case t                                  => throw new NoSuchElementException(s"Unsupported S3 type: '$t' detected. See 'https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html#notification-how-to-event-types-and-destinations' for available events.")
  }
}

trait S3EventName {
  def value: String
}



