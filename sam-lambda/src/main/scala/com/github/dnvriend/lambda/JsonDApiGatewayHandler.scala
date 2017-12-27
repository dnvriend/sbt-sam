package com.github.dnvriend.lambda

import com.github.dnvriend.ops.DisjunctionNel.DisjunctionNel
import play.api.libs.json.Reads

abstract class JsonDApiGatewayHandler[A: Reads] extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    handle(request.bodyAs[A], request, ctx)
  }

  def handle(value: DisjunctionNel[String, A], request: HttpRequest, ctx: SamContext): HttpResponse
}