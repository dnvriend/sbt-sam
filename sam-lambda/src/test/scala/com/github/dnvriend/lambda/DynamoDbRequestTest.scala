package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ DynamoDbUpdateEvent, Generators }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.Json

class DynamoDbRequestTest extends TestSpec with Generators with AllOps {
  it should "parse a dynamodb stream and get new inserted keys's value" in {
    forAll { (event: DynamoDbUpdateEvent) =>
      val input = Json.parse(event.json).toString().toInputStream
      val req: DynamoDbRequest = DynamoDbRequest.parse(input)
      val keys: List[Map[String, Map[String, String]]] = req.getInsertedKeys[Map[String, Map[String, String]]]
      val keyValue: Option[String] = (for {
        id <- keys.flatMap(_.get("Id"))
        nr <- id.get("N")
      } yield nr).headOption
      keyValue.value shouldBe event.value.toString
    }
  }
}
