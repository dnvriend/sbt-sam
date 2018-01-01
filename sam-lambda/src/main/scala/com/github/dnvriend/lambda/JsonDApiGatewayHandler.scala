package com.github.dnvriend.lambda

import com.github.dnvriend.ops.DisjunctionNel.DisjunctionNel
import play.api.libs.json.Reads

abstract class JsonDApiGatewayHandler[A: Reads] extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    handle(
      request.bodyAs[A],
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
    value: DisjunctionNel[String, A],
    pathParams: Map[String, String],
    requestParams: Map[String, String],
    request: HttpRequest,
    ctx: SamContext
  ): HttpResponse
}