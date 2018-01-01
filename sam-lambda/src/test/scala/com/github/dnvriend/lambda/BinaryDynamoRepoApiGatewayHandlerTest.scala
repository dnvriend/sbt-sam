package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ ApiGatewayEventGen, ApiGatewayEventGenBody, Generators }
import com.github.dnvriend.mock.MockBinaryRepository
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.repo.BinaryRepository
import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.MockContext
import play.api.libs.json.{ JsString, Json }

class BinaryDynamoRepoApiGatewayHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle api gateway event" in {
    forAll { (event: ApiGatewayEventGen) =>
      val response: Array[Byte] = withOutputStream { os =>
        val handler = new BinaryDynamoRepoApiGatewayHandler[ApiGatewayEventGenBody]("table") {
          override def createRepository(tableName: String, ctx: SamContext): BinaryRepository = {
            MockBinaryRepository
          }
          override def handle(
            value: Option[ApiGatewayEventGenBody],
            pathParams: Map[String, String],
            requestParams: Map[String, String],
            request: HttpRequest,
            ctx: SamContext,
            repo: BinaryRepository): HttpResponse = {

            value shouldBe 'defined
            HttpResponse.ok.withBody(Json.toJson(value.get))
          }
        }
        handler.handleRequest(event.json.toString.toInputStream, os, MockContext())
      }
      (Json.parse(response) \ "body").get shouldBe JsString(s"""{\"test\":\"${event.value}\"}""")
    }
  }
}
