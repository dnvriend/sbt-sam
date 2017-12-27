package com.github.dnvriend.lambda

import play.api.libs.json._

abstract class JsonApiGatewayHandler[A: Reads] extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    handle(
      request.bodyOpt[A],
      request.pathParamsOpt[Map[String, String]].getOrElse(Map.empty),
      request.requestParamsOpt[Map[String, String]].getOrElse(Map.empty),
      request,
      ctx
    )
  }

  /**
   * Handle an ApiGateway Event
   */
  def handle(
    value: Option[A],
    pathParams: Map[String, String],
    requestParams: Map[String, String],
    request: HttpRequest,
    ctx: SamContext
  ): HttpResponse
}