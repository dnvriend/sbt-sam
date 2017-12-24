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
            1514147985.576,
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

/**
 * {
 * "Records": [
 * {
 * "kinesis": {
 * "kinesisSchemaVersion": "1.0",
 * "partitionKey": "STATIC_PARTITION_KEY",
 * "sequenceNumber": "49580122829133087329639901181121257084264338667950047234",
 * "data": "UGVyc29uKGZvbyk=",
 * "approximateArrivalTimestamp": 1514147985.576
 * },
 * "eventSource": "aws:kinesis",
 * "eventVersion": "1.0",
 * "eventID": "shardId-000000000000:49580122829133087329639901181121257084264338667950047234",
 * "eventName": "aws:kinesis:record",
 * "invokeIdentityArn": "arn:aws:iam::015242279314:role/sam-seed-test1-PersonCreatedKinesisHandlerRole-55Y54D62C08L",
 * "awsRegion": "eu-west-1",
 * "eventSourceARN": "arn:aws:kinesis:eu-west-1:015242279314:stream/sam-seed-test1-person-received"
 * }
 * ]
 * }
 */
