package com.github.dnvriend.lambda.generators

import org.scalacheck.{ Arbitrary, Gen }

final case class KinesisEventGen(json: String, value: String, base64Encoded: String)
trait KinesisEventGenerator {
  def kinesisEventJson(base64Encoded: String): String = {
    s"""
       |{
       |  "Records": [
       |    {
       |      "eventID": "shardId-000000000000:49545115243490985018280067714973144582180062593244200961",
       |      "eventVersion": "1.0",
       |      "kinesis": {
       |        "approximateArrivalTimestamp": 1514147985.576,
       |        "partitionKey": "partitionKey-3",
       |        "data": "$base64Encoded",
       |        "kinesisSchemaVersion": "1.0",
       |        "sequenceNumber": "49545115243490985018280067714973144582180062593244200961"
       |      },
       |      "invokeIdentityArn": "arn:aws:iam::EXAMPLE",
       |      "eventName": "aws:kinesis:record",
       |      "eventSourceARN": "arn:aws:kinesis:EXAMPLE",
       |      "eventSource": "aws:kinesis",
       |      "awsRegion": "us-east-1"
       |    }
       |  ]
       |}
    """.stripMargin
  }
  val kinesisEventGen: Gen[KinesisEventGen] = for {
    value <- Gen.alphaStr
  } yield {
    val base64Encoded: String = java.util.Base64.getEncoder.encodeToString(value.getBytes("UTF-8"))
    KinesisEventGen(kinesisEventJson(base64Encoded), value, base64Encoded)
  }
  implicit val kinesisEventArb: Arbitrary[KinesisEventGen] = Arbitrary(kinesisEventGen)
}
