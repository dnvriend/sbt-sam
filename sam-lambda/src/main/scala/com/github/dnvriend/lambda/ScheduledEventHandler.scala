package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

trait ScheduledEventHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(ScheduledEvent.parse(input), SamContext(context))
  }

  def handle(event: ScheduledEvent, ctx: SamContext): Unit
}
