package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.s3

trait S3EventType {
  def value: String
}
object S3Events {
  case object ObjectCreatedAll extends S3EventType { val value = "s3:ObjectCreated:*" }
  case object ObjectCreatedPut extends S3EventType { val value = "s3:ObjectCreated:Put" }
  case object ObjectCreatedPost extends S3EventType { val value = "s3:ObjectCreated:Post" }
  case object ObjectCreatedCopy extends S3EventType { val value = "s3:ObjectCreated:Copy" }
  case object ObjectCreatedCompleteMultipartUpload extends S3EventType { val value = "s3:ObjectCreated:CompleteMultipartUpload" }
  case object ObjectRemovedAll extends S3EventType { val value = "s3:ObjectRemoved:*" }
  case object ObjectRemovedDelete extends S3EventType { val value = "s3:ObjectRemoved:Delete" }
  case object ObjectRemovedDeleteMarkerCreated extends S3EventType { val value = "s3:ObjectRemoved:DeleteMarkerCreated" }
  case object ReducedRedundancyLostObject extends S3EventType { val value = "s3:ReducedRedundancyLostObject" }

  def fromList(event: List[String]): List[S3EventType] = event.map(withValue)

  def withValue(event: String): S3EventType = event match {
    case "s3:ObjectCreated:*"                       => ObjectCreatedAll
    case "s3:ObjectCreated:Put"                     => ObjectCreatedPut
    case "s3:ObjectCreated:Post"                    => ObjectCreatedPost
    case "s3:ObjectCreated:Copy"                    => ObjectCreatedCopy
    case "s3:ObjectCreated:CompleteMultipartUpload" => ObjectCreatedCompleteMultipartUpload
    case "s3:ObjectRemoved:*"                       => ObjectRemovedAll
    case "s3:ObjectRemoved:Delete"                  => ObjectRemovedDelete
    case "s3:ObjectRemoved:DeleteMarkerCreated"     => ObjectRemovedDeleteMarkerCreated
    case "s3:ReducedRedundancyLostObject"           => ReducedRedundancyLostObject
    case t: String                                  => throw new NoSuchElementException(s"Unsupported S3 type: '$t' detected. See 'https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html#notification-how-to-event-types-and-destinations' for available events.")
  }
}
