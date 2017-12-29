package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.s3

trait S3Event {
  def value: String
}
object S3Events {
  case object ObjectCreatedAll extends S3Event { val value = "s3:ObjectCreated:*" }
  case object ObjectCreatedPut extends S3Event { val value = "s3:ObjectCreated:Put" }
  case object ObjectCreatedPost extends S3Event { val value = "s3:ObjectCreated:Post" }
  case object ObjectCreatedCopy extends S3Event { val value = "s3:ObjectCreated:Copy" }
  case object ObjectCreatedCompleteMultipartUpload extends S3Event { val value = "s3:ObjectCreated:CompleteMultipartUpload" }
  case object ObjectRemovedAll extends S3Event { val value = "s3:ObjectRemoved:*" }
  case object ObjectRemovedDelete extends S3Event { val value = "s3:ObjectRemoved:Delete" }
  case object ObjectRemovedDeleteMarkerCreated extends S3Event { val value = "s3:ObjectRemoved:DeleteMarkerCreated" }
  case object ReducedRedundancyLostObject extends S3Event { val value = "s3:ReducedRedundancyLostObject" }
}
