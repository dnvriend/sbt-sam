package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ Generators, ScheduledEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec

class ScheduledEventTest extends TestSpec with AllOps with Generators {
  it should "parse scheduled event" in {
    forAll { (event: ScheduledEventGen) =>
      ScheduledEvent.parse(event.json.toInputStream) shouldBe
        ScheduledEvent(
          event.account,
          "us-east-1",
          "Scheduled Event",
          "aws.events",
          "1970-01-01T00:00:00Z",
          event.id,
          List("arn:aws:events:us-east-1:123456789012:rule/my-schedule")
        )
    }
  }
}