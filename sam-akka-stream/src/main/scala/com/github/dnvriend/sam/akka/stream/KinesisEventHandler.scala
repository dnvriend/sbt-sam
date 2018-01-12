package com.github.dnvriend.sam.akka.stream

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.github.dnvriend.lambda.{ KinesisEvent, SamContext, KinesisEventHandler => LambdaKinesisEventHandler }

import scala.concurrent.ExecutionContext

/**
 * Handles kinesis events using the reactive streams API
 */
abstract class KinesisEventHandler(implicit ec: ExecutionContext) extends LambdaKinesisEventHandler with AkkaResources {
  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val src: Source[KinesisEvent, NotUsed] = Source.fromIterator(() => events.iterator)
    handle(src, ctx)
  }

  def handle(src: Source[KinesisEvent, NotUsed], ctx: SamContext): Unit
}