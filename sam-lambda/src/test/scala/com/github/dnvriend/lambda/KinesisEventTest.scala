package com.github.dnvriend.lambda

import com.github.dnvriend.lambda.generators.{ Generators, KinesisEventGen }
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec

class KinesisEventTest extends TestSpec with Generators with AllOps {
  it should "parse scheduled event" in {
    forAll { (event: KinesisEventGen) =>
      KinesisEvent.parse(event.json.toInputStream) shouldBe
        List(KinesisEvent(
          "shardId-000000000000:49545115243490985018280067714973144582180062593244200961",
          "1.0",
          Kinesis(
            1428537600,
            "partitionKey-3",
            event.base64Encoded,
            "1.0",
            "49545115243490985018280067714973144582180062593244200961"
          ),
          "arn:aws:iam::EXAMPLE",
          "aws:kinesis:record",
          "arn:aws:kinesis:EXAMPLE",
          "aws:kinesis",
          "us-east-1"
        ))
    }
  }
}