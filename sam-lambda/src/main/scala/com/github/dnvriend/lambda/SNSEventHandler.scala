package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

/**
 * Handles SNS events
 */
trait SNSEventHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(SNSEvent.parse(input), context)
  }
  def handle(events: List[SNSEvent], ctx: Context): Unit
}
