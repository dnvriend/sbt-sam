package com.github.dnvriend.sbt.resource

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.resource.dynamodb.DynamoDBResourceOperations
import com.github.dnvriend.sbt.resource.kinesis.KinesisResourceOperations
import com.github.dnvriend.sbt.resource.policy.PolicyResourceOperations
import com.github.dnvriend.sbt.resource.sns.SNSResourceOperations
import com.typesafe.config.{ Config, ConfigFactory }
import sbt.{ File, _ }

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