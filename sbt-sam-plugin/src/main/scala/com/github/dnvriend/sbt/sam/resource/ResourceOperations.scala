package com.github.dnvriend.sbt.sam.resource

import sbt._

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.dynamodb.DynamoDBResourceOperations
import com.github.dnvriend.sbt.sam.resource.kinesis.KinesisResourceOperations
import com.github.dnvriend.sbt.sam.resource.policy.PolicyResourceOperations
import com.github.dnvriend.sbt.sam.resource.sns.SNSResourceOperations
import com.typesafe.config.{ Config, ConfigFactory }

object ResourceOperations extends ResourceOperations
trait ResourceOperations extends FunctionalOps
  with DynamoDBResourceOperations
  with PolicyResourceOperations
  with SNSResourceOperations
  with KinesisResourceOperations {
  /**
   * Loads the resource configuration from base path
   */
  def readConfig(baseDir: File): Config = {
    ConfigFactory.parseFile(baseDir / "conf" / "sam.conf")
  }
}