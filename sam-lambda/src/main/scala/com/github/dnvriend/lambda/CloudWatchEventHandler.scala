package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

trait CloudWatchEventHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(CloudWatchEvent.parse(input), SamContext(context))
  }

  def handle(event: CloudWatchEvent, ctx: SamContext): Unit
}

