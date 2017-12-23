package com.github.dnvriend.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.github.dnvriend.lambda.generators.{ ApiGatewayEvent, Generators }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ JsString, Json }

class ApiGatewayHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle api gateway event" in {
    forAll { (event: ApiGatewayEvent) =>
      val response: Array[Byte] = withOutputStream { os =>
        val handler = new ApiGatewayHandler {
          override def handle(request: HttpRequest, ctx: Context): HttpResponse = {
            HttpResponse.ok.withBody(request.body)
          }
        }
        handler.handleRequest(event.json.toString.toInputStream, os, null)
      }
      (Json.parse(response) \ "body").get shouldBe JsString(s"""{\"test\":\"${event.value}\"}""")
    }
  }
}
