package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ DynamoDbUpdateEventGen, Generators }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.MockContext

class DynamoDBHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle dynamodb update event" in {
    forAll { (event: DynamoDbUpdateEventGen) =>
      val handler = new DynamoDBHandler {
        override def handle(request: DynamoDbRequest, ctx: SamContext): Unit = {
          request shouldBe DynamoDbRequest.parse(event.json.toInputStream)
        }
      }
      handler.handleRequest(event.json.toInputStream, null, MockContext())
    }
  }
}
