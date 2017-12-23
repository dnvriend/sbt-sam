package com.github.dnvriend.lambda

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ Format, JsString, JsValue, Json }

object TestBody {
  implicit val format: Format[TestBody] = Json.format
}
final case class TestBody(test: String)

class HttpRequestTest extends TestSpec with AllOps with LambdaEventGenerators {
  it should "parse a json inputstream" in {
    forAll { (name: String, age: Int) =>
      val json: JsValue = Json.obj("name" -> name, "age" -> age)
      val input = json.toString.toInputStream
      val req: HttpRequest = HttpRequest.parse(input)
      req shouldBe HttpRequest(json)
    }
  }

  it should "parse http proxy event body" in {
    forAll { (event: ApiGatewayEvent) =>
      val input = Json.parse(event.json).toString().toInputStream
      val req: HttpRequest = HttpRequest.parse(input)
      (req.body \ "test").toOption.value shouldBe JsString(event.value)
      req.body shouldBe Json.obj("test" -> event.value)
      req.bodyOpt[TestBody].value shouldBe TestBody(event.value)
    }
  }

  it should "parse path params" in {
    forAll { (event: ApiGatewayEvent) =>
      val input = Json.parse(event.json).toString().toInputStream
      val req: HttpRequest = HttpRequest.parse(input)
      val pathParams = req.pathParamAs[Map[String, String]].value
      pathParams should not be 'empty
      pathParams.keys should contain("proxy")
      pathParams.get("proxy").value shouldBe "path/to/resource"
      req.pathParameters shouldBe Json.obj("proxy" -> "path/to/resource")
    }
  }

  it should "parse request parameters" in {
    forAll { (event: ApiGatewayEvent) =>
      val input = Json.parse(event.json).toString().toInputStream
      val req: HttpRequest = HttpRequest.parse(input)
      val reqParams = req.requestParamsAs[Map[String, String]].value
      reqParams should not be 'empty
      reqParams.keys should contain("foo")
      reqParams.get("foo").value shouldBe "bar"
      req.requestParameters shouldBe Json.obj("foo" -> "bar")
    }
  }
}
