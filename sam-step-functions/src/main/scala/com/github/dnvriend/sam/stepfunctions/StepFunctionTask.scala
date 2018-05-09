package com.github.dnvriend.sam.stepfunctions

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import com.github.dnvriend.lambda.SamContext
import play.api.libs.json.{ JsValue, Json }

/**
 * Handler for a step function task
 */
trait StepFunctionTask extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val jsResponse = handle(Json.parse(input), SamContext(context))
    val bytes: Array[Byte] = Json.toBytes(jsResponse)
    output.write(bytes)
    output.close()
  }
  def handle(data: JsValue, ctx: SamContext): JsValue
}
