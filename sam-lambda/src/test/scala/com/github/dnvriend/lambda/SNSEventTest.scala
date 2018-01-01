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
          "arn:aws:sns:eu-west-1:015242279314:sam-dynamodb-seed-dnvriend-person-received:2ce66df0-e17d-4818-a764-6b47a764d302",
          "aws:sns",
          SNS(
            "1",
            "2017-12-24T10:39:18.225Z",
            "Cm/9xd83l4LZKQ7LrHsqq6gBoGt9U2n806ycWB0qMcDnIO6YN5/P/Wwyj9/rk/Z+MxgFyQeYBH2RzdiB+j5VEu6T7p/wbH9GgKzNKBHwlr0LnMafLFLdqh5uSH/C1/IfsOEYYwk46QA/qEsn9vLeLxLhDPlJJw7eI9skIjIP1qzM0JOQceGTUxMgorYK8IqRNLixV99QNQ4YhGiqqbR9nC9W/CPI+gRuv6gcnySWUKRXcqmJ/n7POCNUbODWJRNasucNTom7TpAsR9+HzzG0Ucmcos0hJ7iVbwq//SE6F3uJX9X6k6q11OK51DB/Z04ffRKN/ZH0ERdQtauT9zAIfQ==",
            "https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-433026a4050d206028891664da859041.pem",
            "b2ae9ec6-2156-5e66-91f3-cc7fa740f142",
            event.jsonEncoded,
            Map.empty,
            "Notification",
            "https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:015242279314:sam-dynamodb-seed-dnvriend-person-received:2ce66df0-e17d-4818-a764-6b47a764d302",
            "arn:aws:sns:eu-west-1:015242279314:sam-dynamodb-seed-dnvriend-person-received",
            None
          )
        ))
    }
  }
}