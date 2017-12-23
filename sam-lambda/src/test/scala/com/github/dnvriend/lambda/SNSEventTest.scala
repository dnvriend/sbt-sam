package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ Generators, KinesisEventGen, SNSEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec

class SNSEventTest extends TestSpec with Generators with AllOps {
  it should "parse scheduled event" in {
    forAll { (event: SNSEventGen) =>
      SNSEvent.parse(event.json.toInputStream) shouldBe
        List(SNSEvent(
          "1.0",
          "arn:aws:sns:EXAMPLE",
          "aws:sns",
          SNS(
            "1",
            "1970-01-01T00:00:00.000Z",
            "EXAMPLE",
            "EXAMPLE",
            "95df01b4-ee98-5cb9-9903-4c221d41eb5e",
            event.base64Encoded,
            Map(
              "Test" -> Map("Type" -> "String", "Value" -> "TestString"),
              "TestBinary" -> Map("Type" -> "Binary", "Value" -> "TestBinary")
            ),
            "Notification",
            "EXAMPLE",
            "arn:aws:sns:EXAMPLE",
            "TestInvoke"
          )
        ))
    }
  }
}