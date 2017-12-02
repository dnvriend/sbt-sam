package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.s3._
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._

object S3Operations {
  def client(cr: CredentialsAndRegion): AmazonS3 = {
    AmazonS3ClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }
}
