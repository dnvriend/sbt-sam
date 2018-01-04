package com.github.dnvriend.lambda.generators

import org.scalacheck.{ Arbitrary, Gen }
import play.api.libs.json.Json

final case class SNSEventGen(json: String, name: String, age: Int, jsonEncoded: String, escapedJsonEncoded: String)
trait SNSEventGenerator {
  def snsEventJson(escapedJsonEncoded: String): String = {
    s"""
       |{
       |    "Records": [
       |        {
       |            "EventSource": "aws:sns",
       |            "EventVersion": "1.0",
       |            "EventSubscriptionArn": "arn:aws:sns:eu-west-1:015242279314:sam-dynamodb-seed-dnvriend-person-received:2ce66df0-e17d-4818-a764-6b47a764d302",
       |            "Sns": {
       |                "Type": "Notification",
       |                "MessageId": "b2ae9ec6-2156-5e66-91f3-cc7fa740f142",
       |                "TopicArn": "arn:aws:sns:eu-west-1:015242279314:sam-dynamodb-seed-dnvriend-person-received",
       |                "Subject": null,
       |                "Message": $escapedJsonEncoded,
       |                "Timestamp": "2017-12-24T10:39:18.225Z",
       |                "SignatureVersion": "1",
       |                "Signature": "Cm/9xd83l4LZKQ7LrHsqq6gBoGt9U2n806ycWB0qMcDnIO6YN5/P/Wwyj9/rk/Z+MxgFyQeYBH2RzdiB+j5VEu6T7p/wbH9GgKzNKBHwlr0LnMafLFLdqh5uSH/C1/IfsOEYYwk46QA/qEsn9vLeLxLhDPlJJw7eI9skIjIP1qzM0JOQceGTUxMgorYK8IqRNLixV99QNQ4YhGiqqbR9nC9W/CPI+gRuv6gcnySWUKRXcqmJ/n7POCNUbODWJRNasucNTom7TpAsR9+HzzG0Ucmcos0hJ7iVbwq//SE6F3uJX9X6k6q11OK51DB/Z04ffRKN/ZH0ERdQtauT9zAIfQ==",
       |                "SigningCertUrl": "https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-433026a4050d206028891664da859041.pem",
       |                "UnsubscribeUrl": "https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:015242279314:sam-dynamodb-seed-dnvriend-person-received:2ce66df0-e17d-4818-a764-6b47a764d302",
       |                "MessageAttributes": {}
       |            }
       |        }
       |    ]
       |}
    """.stripMargin
  }
  val snsEventGen: Gen[SNSEventGen] = for {
    name <- Gen.alphaStr
    age <- Gen.posNum[Int]
  } yield {
    val jsonEncoded = Json.obj("name" -> name, "age" -> age).toString
    val escapedJsonEncoded: String = Json.toJson(jsonEncoded).toString
    SNSEventGen(snsEventJson(escapedJsonEncoded), name, age, jsonEncoded, escapedJsonEncoded)
  }
  implicit val snsEventArb: Arbitrary[SNSEventGen] = Arbitrary(snsEventGen)
}