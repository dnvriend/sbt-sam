package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model.Bucket

import scala.collection.JavaConverters._

final case class S3BucketId(value: String)

object S3Operations {
  def client(cr: CredentialsAndRegion): AmazonS3 = {
    AmazonS3ClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  def getBucket(bucketId: S3BucketId, client: AmazonS3): Option[Bucket] = {
    client.listBuckets().asScala.find(_.getName == bucketId.value)
  }
}
