package com.github.dnvriend.dynamodb.repo

import com.github.dnvriend.lambda.{ ApiGatewayHandler, HttpRequest, HttpResponse, SamContext }
import play.api.libs.json.Format

abstract class BinaryRepositoryApiGatewayHandler[A: Format](tableName: String) extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    val repo = new BinaryRepository(tableName, ctx)
    handle(request.bodyOpt[A], repo, request, ctx)
  }

  def handle(value: Option[A], repo: BinaryRepository, request: HttpRequest, ctx: SamContext): HttpResponse
}
