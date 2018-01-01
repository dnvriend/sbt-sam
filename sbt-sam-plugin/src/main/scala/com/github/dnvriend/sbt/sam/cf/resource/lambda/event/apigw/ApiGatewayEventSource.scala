package com.github.dnvriend.sbt.sam.cf.resource.lambda.event.apigw

import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import play.api.libs.json.{Json, Writes}

object ApiGatewayEventSource {
  final val ServerlessRestApi: String = "ServerlessRestApi"
  implicit val writes: Writes[ApiGatewayEventSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "Api",
        "Properties" -> Json.obj(
          "Path" -> path,
          "Method" -> method,
          "RestApiId" -> Json.obj("Ref" -> ServerlessRestApi)
        )
      ))
  })
}
case class ApiGatewayEventSource(
                                logicalResourceId: String,
                                path: String,
                                method: String,
                                ) extends EventSource

