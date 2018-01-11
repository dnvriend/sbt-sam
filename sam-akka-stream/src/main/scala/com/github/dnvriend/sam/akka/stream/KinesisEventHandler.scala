package com.github.dnvriend.sam.akka.stream

import java.nio.ByteBuffer

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import com.amazonaws.services.kinesis.model.{ PutRecordsRequest, PutRecordsRequestEntry }
import com.amazonaws.services.kinesis.{ AmazonKinesis, AmazonKinesisClientBuilder }
import com.github.dnvriend.lambda.{ KinesisEvent, SamContext, KinesisEventHandler => LambdaKinesisEventHandler }
import play.api.libs.json.JsValue

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

/**
 * Handles kinesis events
 */
abstract class KinesisEventHandler(implicit ec: ExecutionContext) extends LambdaKinesisEventHandler with AkkaResources {
  def outputStreamName(stage: String): String

  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val kinesis: AmazonKinesis = AmazonKinesisClientBuilder.defaultClient()
    val src: Source[KinesisEvent, NotUsed] = Source.fromIterator(() => events.iterator)
    val xs: List[(String, JsValue)] = Await.result(handle(src, ctx), 4.minutes)
    val records: List[PutRecordsRequestEntry] = xs.map({
      case (key, js) =>
        new PutRecordsRequestEntry()
          .withPartitionKey(key)
          .withData(ByteBuffer.wrap(js.toString().getBytes("UTF-8")))
    })
    kinesis.putRecords(new PutRecordsRequest()
      .withStreamName(outputStreamName(ctx.stage))
      .withRecords(records: _*)
    )
  }

  implicit def sourceToFuture(that: Source[(String, JsValue), _]): Future[List[(String, JsValue)]] = {
    that.runWith(Sink.seq).map(_.toList)
  }

  def handle(src: Source[KinesisEvent, NotUsed], ctx: SamContext): Future[List[(String, JsValue)]]
}
