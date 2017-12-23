package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.Generators
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ JsNull, JsValue }

class HttpResponseTest extends TestSpec with Generators {
  it should "support response codes" in {
    HttpResponse.ok shouldBe HttpResponse(200, JsNull, Map.empty)
    HttpResponse.validationError shouldBe HttpResponse(400, JsNull, Map.empty)
    HttpResponse.notFound shouldBe HttpResponse(404, JsNull, Map.empty)
    HttpResponse.serverError shouldBe HttpResponse(500, JsNull, Map.empty)
  }

  it should "create response with body" in {
    forAll { (body: JsValue) =>
      HttpResponse
        .ok
        .withBody(body)
        .withHeader("foo", "bar") shouldBe HttpResponse(200, body, Map("foo" -> "bar"))
    }
  }

  it should "alter existing response status" in {
    HttpResponse.serverError.ok shouldBe HttpResponse(200, JsNull, Map.empty)
    HttpResponse.serverError.validationError shouldBe HttpResponse(400, JsNull, Map.empty)
    HttpResponse.serverError.notFound shouldBe HttpResponse(404, JsNull, Map.empty)
    HttpResponse.ok.serverError shouldBe HttpResponse(500, JsNull, Map.empty)
  }
}
