package com.github.dnvriend.dynamodb.repo

import com.github.dnvriend.lambda.{ ApiGatewayHandler, HttpRequest, HttpResponse, SamContext }
import play.api.libs.json.Format

abstract class JsonRepositoryApiGatewayHandler[A: Format](tableName: String) extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    val repo = new JsonRepository(tableName, ctx)
    handle(request.bodyOpt[A], repo, request, ctx)
  }

  def handle(value: Option[A], repo: JsonRepository, request: HttpRequest, ctx: SamContext): HttpResponse
}
