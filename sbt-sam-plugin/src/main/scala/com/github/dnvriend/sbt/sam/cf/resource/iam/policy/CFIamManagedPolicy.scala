package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

sealed trait CFIamManagedPolicy { def arn: String }
object CFIamManagedPolicy {

  /**
   * Returns the arn for the managed policy
   */
  def getArn(policy: CFIamManagedPolicy): String = {
    s"arn:aws:iam::aws:policy/$policy"
  }

  /**
   * Provides full access to Lambda, S3, DynamoDB, CloudWatch Metrics and Logs.
   */
  case object AWSLambdaFullAccess extends CFIamManagedPolicy {
    def arn: String = getArn(this)
  }

  /**
   * Provides full access to all buckets via the AWS Management Console.
   */
  case object AmazonS3FullAccess extends CFIamManagedPolicy {
    def arn: String = getArn(this)
  }

  /**
   * Provides full access to all streams via the AWS Management Console.
   */
  case object AmazonKinesisFullAccess extends CFIamManagedPolicy {
    def arn: String = getArn(this)
  }

  /**
   * Provides full access to all Amazon Kinesis Firehose Delivery Streams.
   */
  case object AmazonKinesisFirehoseFullAccess extends CFIamManagedPolicy {
    def arn: String = getArn(this)
  }
}