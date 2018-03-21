package com.github.dnvriend.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import play.api.libs.json.{ JsValue, Json }

/**
 * Generic handler parses all events as JsValue and provides the event as JsValue and the Lambda Context
 */
trait GenericHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    handle(Json.parse(input), SamContext(context))
  }

  def handle(event: JsValue, ctx: SamContext): Unit
}
