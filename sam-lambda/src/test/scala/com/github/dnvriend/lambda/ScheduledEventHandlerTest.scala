package com.github.dnvriend.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.github.dnvriend.lambda.generators.{ Generators, ScheduledEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec

class ScheduledEventHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle scheduled event" in {
    forAll { (event: ScheduledEventGen) =>
      val handler = new ScheduledEventHandler {
        override def handle(request: ScheduledEvent, ctx: Context): Unit = {
          request shouldBe ScheduledEvent.parse(event.json.toInputStream)
        }
      }
      handler.handleRequest(event.json.toInputStream, null, null)
    }
  }
}
