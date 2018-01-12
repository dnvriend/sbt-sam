package com.github.dnvriend.sam.akka.stream

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import com.amazonaws.services.kinesis.model.PutRecordsResultEntry
import com.github.dnvriend.kinesis.{ KinesisProducer, KinesisRecord }
import com.github.dnvriend.lambda.{ KinesisEvent, SamContext, KinesisEventHandler => LambdaKinesisEventHandler }

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scalaz.std.AllInstances._
import scalaz.syntax.foldable._

/**
 * A Kinesis Event Processor that handles kinesis events and sends kinesis events to the outputStreamName.
 */
abstract class KinesisEventProcessor(outputStreamName: String)(implicit ec: ExecutionContext) extends LambdaKinesisEventHandler with AkkaResources {
  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val src: Source[KinesisEvent, NotUsed] = Source.fromIterator(() => events.iterator)
    val records: List[KinesisRecord] = Await.result(handle(src, ctx), 4.minutes)
    val result: List[PutRecordsResultEntry] = KinesisProducer(ctx).produce(outputStreamName, records)
    ctx.logger.log(result.foldMap(_.toString))
  }

  implicit def sourceToFuture(that: Source[KinesisRecord, _]): Future[List[KinesisRecord]] = {
    that.runWith(Sink.seq).map(_.toList)
  }

  def handle(src: Source[KinesisEvent, NotUsed], ctx: SamContext): Future[List[KinesisRecord]]
}