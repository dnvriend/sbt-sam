package com.github.dnvriend.sbt.sam.resource

import sbt._
import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.bucket.S3BucketResourceOperations
import com.github.dnvriend.sbt.sam.resource.cognito.CognitoResourceOperations
import com.github.dnvriend.sbt.sam.resource.dynamodb.DynamoDBResourceOperations
import com.github.dnvriend.sbt.sam.resource.firehose.s3.S3FirehoseResourceOperations
import com.github.dnvriend.sbt.sam.resource.kinesis.KinesisResourceOperations
import com.github.dnvriend.sbt.sam.resource.rds.RDSResourceOperations
import com.github.dnvriend.sbt.sam.resource.role.RoleResourceOperations
import com.github.dnvriend.sbt.sam.resource.sns.SNSResourceOperations
import com.typesafe.config.{ Config, ConfigFactory }

object ResourceOperations extends ResourceOperations

trait ResourceOperations extends FunctionalOps
  with CognitoResourceOperations
  with DynamoDBResourceOperations
  with SNSResourceOperations
  with KinesisResourceOperations
  with S3BucketResourceOperations
  with S3FirehoseResourceOperations
  with RoleResourceOperations
  with RDSResourceOperations {
  /**
   * Loads the resource configuration from base path
   */
  def readConfig(baseDir: File): Config = {
    ConfigFactory.parseFile(baseDir / "conf" / "sam.conf")
  }
}