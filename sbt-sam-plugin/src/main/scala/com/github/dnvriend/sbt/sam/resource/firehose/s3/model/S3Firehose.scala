package com.github.dnvriend.sbt.sam.resource.firehose.s3.model

import com.github.dnvriend.sbt.sam.cf.resource.iam.policy.CFIamManagedPolicy
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import com.github.dnvriend.sbt.sam.resource.role.model.IamRole

/**
  * Defines an S3 Extended Kinesis Data Firehose Resource that consists of
  * an S3 Bucket, IAMRole, Kinesis Stream and of course the Kinesis Data Firehose resource
  *
  * read: https://docs.aws.amazon.com/firehose/latest/dev/troubleshooting.html
  * read: https://docs.aws.amazon.com/firehose/latest/dev/controlling-access.html
  *
  */
case class S3Firehose(
                     name: String,
                     configName: String = "",
                     compression: String = "uncompressed",
                     shardCount: Int = 1,
                     retentionPeriodHours: Int = 24,
                     bufferingIntervalInSeconds: Int = 300,
                     bufferingSize: Int = 5,
                     encryptionKey: Option[String] = None,
                     export: Boolean = false,
                     ) {

  def firehoseName(projectName: String, stage: String): String = s"$projectName-$stage-$name".toLowerCase.trim
  def roleName(projectName: String, stage: String): String = s"${firehoseName(projectName, stage)}-role"
  def roleLogicalName: String = s"${configName}Role"
  def bucketName(projectName: String, stage: String): String = s"${firehoseName(projectName, stage)}-bucket"
  def bucketLogicalName: String = s"${configName}Bucket"
  def streamName(projectName: String, stage: String): String = s"${firehoseName(projectName, stage)}-stream"
  def streamLogicalName: String = s"${configName}Stream"

  /**
    * Returns an S3Bucket resource
    */
  def bucket(projectName: String, stage: String): S3Bucket = {
    S3Bucket(
      bucketName(projectName, stage),
      "BucketOwnerFullControl",
      bucketLogicalName,
      None,
      false,
      false,
      false,
      export
    )
  }

  /**
    * Returns an IAMRole resource
    */
  def role(projectName: String, stage: String): IamRole = {
    IamRole(
      roleName(projectName, stage),
      roleLogicalName,
      "firehose.amazonaws.com",
      List(
        CFIamManagedPolicy.AmazonKinesisFirehoseFullAccess.arn,
        CFIamManagedPolicy.AmazonKinesisFullAccess.arn,
        CFIamManagedPolicy.AmazonS3FullAccess.arn,
        CFIamManagedPolicy.AWSLambdaFullAccess.arn,
        CFIamManagedPolicy.CloudWatchFullAccess.arn,
      ),
      false
    )
  }

  /**
    * Returns a kinesis strema resource
    */
  def stream(projectName: String, stage: String): KinesisStream = {
    KinesisStream(
      streamName(projectName, stage),
      streamLogicalName,
      retentionPeriodHours,
      shardCount,
      export
    )
  }
}