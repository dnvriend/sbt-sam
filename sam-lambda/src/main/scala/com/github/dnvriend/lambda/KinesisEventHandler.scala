package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import com.github.dnvriend.ops.AllOps

/**
 * Handles kinesis events
 */
trait KinesisEventHandler extends RequestStreamHandler with AllOps {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(KinesisEvent.parse(input), SamContext(context))
  }
  def handle(events: List[KinesisEvent], ctx: SamContext): Unit
}
