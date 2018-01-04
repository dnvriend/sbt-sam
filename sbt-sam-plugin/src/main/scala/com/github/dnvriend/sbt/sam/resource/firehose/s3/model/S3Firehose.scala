package com.github.dnvriend.sbt.sam.resource.firehose.s3.model

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import com.github.dnvriend.sbt.sam.resource.role.model.{IamPolicyAllow, IamRole}
import com.github.dnvriend.sbt.sam.task.CloudFormationTemplates

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
                       compression: String,
                       shardCount: Int,
                       retentionPeriodHours: Int,
                       bufferingIntervalInSeconds: Int,
                       bufferingSize: Int,
                       export: Boolean = false,
                     ) {

  def roleName: String = s"$name-role"

  def roleLogicalName: String = s"${configName}Role"

  def bucketName: String = s"$name-bucket"

  def bucketLogicalName: String = s"${configName}Bucket"

  def streamName: String = s"$name-stream"

  def streamLogicalName: String = s"${configName}Stream"

  def logGroupName: String = s"$name-log-group"

  def logGroupLogicalName: String = s"${configName}LogGroup"

  /**
    * Returns an S3Bucket resource
    */
  def bucket(projectName: String, stage: String): S3Bucket = {
    S3Bucket(
      bucketName,
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
    * Returns a kinesis stream resource
    */
  def stream(projectName: String, stage: String): KinesisStream = {
    KinesisStream(
      streamName,
      streamLogicalName,
      retentionPeriodHours,
      shardCount,
      export
    )
  }

  /**
    * Returns an IAMRole resource
    */
  def role(projectName: String, stage: String, accountId: String, region: String): IamRole = {
    IamRole(
      roleName,
      roleLogicalName,
      "firehose.amazonaws.com",
      List.empty,
      List(
        IamPolicyAllow(
          s"$roleName-s3-access-policy",
          List(
            "s3:AbortMultipartUpload",
            "s3:GetBucketLocation",
            "s3:GetObject",
            "s3:ListBucket",
            "s3:ListBucketMultipartUploads",
            "s3:PutObject"
          ),
          List(
            CloudFormation.bucketArn(CloudFormationTemplates.createResourceName(projectName, stage, bucketName)),
            CloudFormation.bucketArn(CloudFormationTemplates.createResourceName(projectName, stage, bucketName)) + "/*"
          )
        ),
        IamPolicyAllow(
          s"$roleName-kinesis-access-policy",
          List(
            "kinesis:DescribeStream",
            "kinesis:GetShardIterator",
            "kinesis:GetRecords"
          ),
          List(
            CloudFormation.kinesisArn(accountId, region, CloudFormationTemplates.createResourceName(projectName, stage, streamName))
          )
        )
      )
    )
  }
}