package com.github.dnvriend.lambda

import com.github.dnvriend.repo.BinaryRepository
import com.github.dnvriend.repo.dynamodb.DynamoDBBinaryRepository
import play.api.libs.json.Reads

abstract class BinaryDynamoRepoApiGatewayHandler[A: Reads](tableName: String) extends ApiGatewayHandler {
  /**
   * Creates a BinaryRepository
   */
  def createRepository(tableName: String, ctx: SamContext): BinaryRepository = {
    new DynamoDBBinaryRepository(tableName, ctx)
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
    repo: BinaryRepository
  ): HttpResponse
}
