package com.github.dnvriend.lambda

import com.github.dnvriend.repo.JsonRepository
import com.github.dnvriend.repo.dynamodb.DynamoDBJsonRepository
import play.api.libs.json.Format

abstract class JsonDynamoRepoApiGatewayHandler[A: Format](tableName: String) extends ApiGatewayHandler {
  /**
   * Creates a JsonRepository
   */
  def createRepository(tableName: String, ctx: SamContext): JsonRepository = {
    new DynamoDBJsonRepository(tableName, ctx)
  }

  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    handle(
      request.bodyOpt[A],
      request.pathParamsOpt[Map[String, String]].getOrElse(Map.empty),
      request.requestParamsOpt[Map[String, String]].getOrElse(Map.empty),
      request,
      ctx,
      createRepository(tableName, ctx)
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
    ctx: SamContext,
    repo: JsonRepository
  ): HttpResponse
}
