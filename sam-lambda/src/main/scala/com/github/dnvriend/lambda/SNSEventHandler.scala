package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

/**
 * Handles SNS events
 */
trait SNSEventHandler extends RequestStreamHandler with XRayTracer {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val event = withSubsegment("parseEvent", _ => SNSEvent.parse(input))
    withSubsegment("handleEvent", _ => handle(event, SamContext(context)))
  }
  def handle(event: SNSEvent, ctx: SamContext): Unit
}