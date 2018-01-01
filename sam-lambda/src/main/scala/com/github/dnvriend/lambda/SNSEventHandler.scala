package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import com.github.dnvriend.ops.AllOps

/**
 * Handles SNS events
 */
trait SNSEventHandler extends RequestStreamHandler with AllOps {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(SNSEvent.parse(input), SamContext(context))
  }
  def handle(events: List[SNSEvent], ctx: SamContext): Unit
}