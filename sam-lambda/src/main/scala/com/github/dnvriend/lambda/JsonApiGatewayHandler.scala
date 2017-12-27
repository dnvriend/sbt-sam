package com.github.dnvriend.lambda

import play.api.libs.json.Reads

abstract class JsonApiGatewayHandler[A: Reads] extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    handle(request.bodyOpt[A], request, ctx)
  }

  def handle(value: Option[A], request: HttpRequest, ctx: SamContext): HttpResponse
}