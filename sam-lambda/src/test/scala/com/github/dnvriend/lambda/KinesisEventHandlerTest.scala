package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ Generators, KinesisEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.MockContext

class KinesisEventHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle kinesis event" in {
    forAll { (kinesisEvent: KinesisEventGen) =>
      val handler = new KinesisEventHandler {
        override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
          events shouldBe KinesisEvent.parse(kinesisEvent.json.toInputStream)
        }
      }
      handler.handleRequest(kinesisEvent.json.toInputStream, null, MockContext())
    }
  }
}
