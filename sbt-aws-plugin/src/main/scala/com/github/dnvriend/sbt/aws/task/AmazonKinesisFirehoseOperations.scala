package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.kinesisfirehose.model._
import com.amazonaws.services.kinesisfirehose._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Try }

object AmazonKinesisFirehoseOperations {
  def client(): AmazonKinesisFirehose = {
    AmazonKinesisFirehoseClientBuilder.defaultClient()
  }

  /**
   * Returns a list of delivery stream names.
   */
  def listDeliveryStreams(client: AmazonKinesisFirehose): List[String] = {
    client.listDeliveryStreams(new ListDeliveryStreamsRequest()
      .withLimit(100)
    ).getDeliveryStreamNames.asScala.toList
  }

  /**
   * Describes the specified delivery stream and gets the status.
   */
  def findDeliveryStream(deliveryStreamName: String, client: AmazonKinesisFirehose): Option[DeliveryStreamDescription] = Try {
    client.describeDeliveryStream(new DescribeDeliveryStreamRequest()
      .withDeliveryStreamName(deliveryStreamName)
      .withLimit(1)
    ).getDeliveryStreamDescription
  }.recoverWith {
    case t: Throwable =>
      println(t.getMessage)
      Failure(t)
  }.toOption
}
