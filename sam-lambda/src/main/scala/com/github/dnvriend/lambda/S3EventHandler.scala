package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }

trait S3EventHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(S3Event.parse(input), SamContext(context))
  }

  def handle(events: List[S3Event], ctx: SamContext): Unit
}
