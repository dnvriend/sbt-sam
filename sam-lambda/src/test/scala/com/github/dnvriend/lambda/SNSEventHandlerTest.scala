package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ Generators, SNSEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.MockContext

class SNSEventHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle sns event" in {
    forAll { (snsEvent: SNSEventGen) =>
      val handler = new SNSEventHandler {
        override def handle(events: List[SNSEvent], ctx: SamContext): Unit = {
          events shouldBe SNSEvent.parse(snsEvent.json.toInputStream)
        }
      }
      handler.handleRequest(snsEvent.json.toInputStream, null, MockContext())
    }
  }
}
