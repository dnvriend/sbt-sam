package com.github.dnvriend.lambda

import com.github.dnvriend.repo.dynamodb.DynamoDBJsonRepository
import play.api.libs.json.Format

abstract class JsonDynamoRepoApiGatewayHandler[A: Format](tableName: String) extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    val repo = new DynamoDBJsonRepository(tableName, ctx)
    handle(request.bodyOpt[A], repo, request, ctx)
  }

  def handle(value: Option[A], repo: DynamoDBJsonRepository, request: HttpRequest, ctx: SamContext): HttpResponse
}
