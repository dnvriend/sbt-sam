package com.github.dnvriend.sns

import com.amazonaws.services.sns.model.PublishResult
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }
import com.github.dnvriend.lambda.SamContext

object TopicProducer {
  def apply(ctx: SamContext): TopicProducer = {
    new TopicProducer(ctx)
  }
}

/**
 * TopicProducer produces a message of type 'String' to a topic based
 * on a topic name. The topic name supports the 'import' syntax and the topic
 * name will be ultimately resolved to a topic arn to be used. The client only
 * has to supply the topicName or the import syntax of the component that manages
 * and exports the topic name.
 */
class TopicProducer(ctx: SamContext) {
  private val client: AmazonSNS = AmazonSNSClientBuilder.defaultClient()

  def produce(topicName: String, message: String): PublishResult = {
    client.publish(ctx.snsTopicArn(topicName), message)
  }
}
