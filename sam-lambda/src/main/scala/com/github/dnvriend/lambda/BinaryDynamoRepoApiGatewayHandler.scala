package com.github.dnvriend.lambda

import com.github.dnvriend.repo.dynamodb.DynamoDBBinaryRepository
import play.api.libs.json.Format

abstract class BinaryDynamoRepoApiGatewayHandler[A: Format](tableName: String) extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    val repo = new DynamoDBBinaryRepository(tableName, ctx)
    handle(request.bodyOpt[A], repo, request, ctx)
  }

  def handle(value: Option[A], repo: DynamoDBBinaryRepository, request: HttpRequest, ctx: SamContext): HttpResponse
}
