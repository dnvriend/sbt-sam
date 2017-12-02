package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.kinesis._
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._

/**
 * add-tags-to-stream                       | create-stream
 * decrease-stream-retention-period         | delete-stream
 * describe-limits                          | describe-stream
 * describe-stream-summary                  | disable-enhanced-monitoring
 * enable-enhanced-monitoring               | get-records
 * get-shard-iterator                       | increase-stream-retention-period
 * list-streams                             | list-tags-for-stream
 * merge-shards                             | put-record
 * put-records                              | remove-tags-from-stream
 * split-shard                              | start-stream-encryption
 * stop-stream-encryption                   | update-shard-count
 */
object KinesisOperations {
  def client(cr: CredentialsAndRegion): AmazonKinesis = {
    AmazonKinesisClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }
}
