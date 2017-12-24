package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ Generators, ScheduledEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.MockContext

class ScheduledEventHandlerTest extends TestSpec with Generators with AllOps {
  it should "handle scheduled event" in {
    forAll { (scheduledEvent: ScheduledEventGen) =>
      val handler = new ScheduledEventHandler {
        override def handle(event: ScheduledEvent, ctx: SamContext): Unit = {
          event shouldBe ScheduledEvent.parse(scheduledEvent.json.toInputStream)
        }
      }
      handler.handleRequest(scheduledEvent.json.toInputStream, null, MockContext())
    }
  }
}
