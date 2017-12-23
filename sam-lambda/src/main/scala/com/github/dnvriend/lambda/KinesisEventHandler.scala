package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

/**
 * Handles kinesis events
 */
trait KinesisEventHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(KinesisEvent.parse(input), context)
  }
  def handle(events: List[KinesisEvent], ctx: Context): Unit
}
