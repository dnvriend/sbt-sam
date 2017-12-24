package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ ApiGatewayEventGen, Generators }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.MockContext
import play.api.libs.json.{ JsString, Json }

class ApiGatewayHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle api gateway event" in {
    forAll { (event: ApiGatewayEventGen) =>
      val response: Array[Byte] = withOutputStream { os =>
        val handler = new ApiGatewayHandler {
          override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
            HttpResponse.ok.withBody(request.body)
          }
        }
        handler.handleRequest(event.json.toString.toInputStream, os, MockContext())
      }
      (Json.parse(response) \ "body").get shouldBe JsString(s"""{\"test\":\"${event.value}\"}""")
    }
  }
}
