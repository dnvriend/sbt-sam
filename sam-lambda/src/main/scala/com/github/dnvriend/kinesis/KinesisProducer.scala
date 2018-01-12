package com.github.dnvriend.kinesis

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.model.{ PutRecordResult, PutRecordsRequest, PutRecordsRequestEntry, PutRecordsResultEntry }
import com.amazonaws.services.kinesis.{ AmazonKinesis, AmazonKinesisClientBuilder }
import com.github.dnvriend.lambda.SamContext

import scala.collection.JavaConverters._

case class KinesisRecord(partitionKey: String, data: Array[Byte])

object KinesisProducer {
  def apply(ctx: SamContext): KinesisProducer = {
    new KinesisProducer(ctx)
  }
}

/**
 * Kinesis Producer for producing messages to kinesis streams.
 * The Kinesis Producer uses one or more 'KinesisRecord' instances
 * to encode data to send and the partition to send the data to.
 * @param ctx
 */
class KinesisProducer(ctx: SamContext) {
  private val kinesis: AmazonKinesis = AmazonKinesisClientBuilder.defaultClient()

  def produce(topicName: String, record: KinesisRecord): PutRecordResult = {
    kinesis.putRecord(ctx.kinesisStreamName(topicName), ByteBuffer.wrap(record.data), record.partitionKey)
  }

  def produce(topicName: String, records: List[KinesisRecord]): List[PutRecordsResultEntry] = {
    val xs: List[PutRecordsRequestEntry] = records.map {
      case KinesisRecord(key, data) =>
        new PutRecordsRequestEntry()
          .withPartitionKey(key)
          .withData(ByteBuffer.wrap(data))
    }
    kinesis.putRecords(new PutRecordsRequest()
      .withStreamName(ctx.kinesisStreamName(topicName))
      .withRecords(xs: _*)
    ).getRecords.asScala.toList
  }
}
