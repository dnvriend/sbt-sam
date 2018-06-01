package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

/**
 * Handles kinesis events
 */
trait KinesisEventHandler extends RequestStreamHandler with XRayTracer {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val events = withSubsegment("parseEvents", _ => KinesisEvent.parse(input))
    withSubsegment("handleEvents", _ => handle(events, SamContext(context)))
  }
  def handle(events: List[KinesisEvent], ctx: SamContext): Unit
}
