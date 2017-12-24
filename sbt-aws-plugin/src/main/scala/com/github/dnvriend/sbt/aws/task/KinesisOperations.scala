package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.kinesis._
import com.amazonaws.services.kinesis.model.StreamDescription
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._
import scala.util.Try

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
  def client(): AmazonKinesis = {
    AmazonKinesisClientBuilder.defaultClient()
  }

  /**
   * The information returned includes the stream name, Amazon Resource Name (ARN), creation time, enhanced metric configuration,
   * and shard map. The shard map is an array of shard objects. For each shard object, there is the hash key and sequence number
   * ranges that the shard spans, and the IDs of any earlier shards that played in a role in creating the shard. Every record ingested
   * in the stream is identified by a sequence number, which is assigned when the record is put into the stream.
   */
  def describeStream(streamName: String, client: AmazonKinesis): Option[StreamDescription] = {
    Try(client.describeStream(streamName).getStreamDescription).toOption
  }
}
