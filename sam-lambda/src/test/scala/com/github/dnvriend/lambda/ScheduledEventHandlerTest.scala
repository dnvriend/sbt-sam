package com.github.dnvriend.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.github.dnvriend.lambda.generators.{ Generators, ScheduledEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec

class ScheduledEventHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle scheduled event" in {
    forAll { (scheduledEvent: ScheduledEventGen) =>
      val handler = new ScheduledEventHandler {
        override def handle(event: ScheduledEvent, ctx: Context): Unit = {
          event shouldBe ScheduledEvent.parse(scheduledEvent.json.toInputStream)
        }
      }
      handler.handleRequest(scheduledEvent.json.toInputStream, null, null)
    }
  }
}
