package com.github.dnvriend.lambda.generators

import org.scalacheck.{ Arbitrary, Gen }

final case class ScheduledEventGen(json: String, account: String, id: String)
trait ScheduledEventGenerator {
  def scheduledEventJson(account: String, id: String): String = {
    s"""
       |{
       |  "account": "$account",
       |  "region": "us-east-1",
       |  "detail": {},
       |  "detail-type": "Scheduled Event",
       |  "source": "aws.events",
       |  "time": "1970-01-01T00:00:00Z",
       |  "id": "$id",
       |  "resources": [
       |    "arn:aws:events:us-east-1:123456789012:rule/my-schedule"
       |  ]
       |}
    """.stripMargin
  }
  val scheduledEventGen: Gen[ScheduledEventGen] = for {
    account <- Gen.alphaStr
    id <- Gen.uuid
  } yield ScheduledEventGen(scheduledEventJson(account, id.toString), account, id.toString)
  implicit val scheduledEventArb: Arbitrary[ScheduledEventGen] = Arbitrary(scheduledEventGen)
}

