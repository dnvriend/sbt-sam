package com.github.dnvriend.sam.akka.stream

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.github.dnvriend.lambda.{ HttpRequest, HttpResponse, SamContext, JsonApiGatewayHandler => LambdaJsonApiGatewayHandler }
import play.api.libs.json.{ Json, Reads, Writes }

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

abstract class JsonApiGatewayHandler[A: Reads](implicit ec: ExecutionContext) extends LambdaJsonApiGatewayHandler[A] with AkkaResources {
  override def handle(
    value: Option[A],
    pathParams: Map[String, String],
    requestParams: Map[String, String],
    request: HttpRequest,
    ctx: SamContext): HttpResponse = {
    val src: Source[A, NotUsed] = Source.fromIterator(() => value.iterator)
    Await.result(handle(src, pathParams, requestParams, request, ctx), 5.minutes)
  }

  implicit def sourceToFuture(that: Source[HttpResponse, _]): Future[HttpResponse] = {
    that.runWith(Sink.last[HttpResponse])
  }

  def mapResponse[B: Writes]: Flow[B, HttpResponse, NotUsed] = {
    Flow.apply[B]
      .map(Json.toJson(_))
      .map(HttpResponse.ok.withBody)
      .recover({ case t: Throwable => HttpResponse.serverError.withBody(Json.toJson(t.getMessage)) })
  }

  def handle(
    src: Source[A, NotUsed],
    pathParams: Map[String, String],
    requestParams: Map[String, String],
    request: HttpRequest,
    ctx: SamContext): Future[HttpResponse]
}
