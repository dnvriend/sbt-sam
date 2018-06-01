package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

trait S3EventHandler extends RequestStreamHandler with XRayTracer {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val events = withSubsegment("parseEvents", _ => S3Event.parse(input))
    withSubsegment("handleEvents", _ => handle(events, SamContext(context)))
  }

  def handle(events: List[S3Event], ctx: SamContext): Unit
}
