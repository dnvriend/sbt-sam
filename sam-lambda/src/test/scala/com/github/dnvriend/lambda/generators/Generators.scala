package com.github.dnvriend.lambda.generators

import org.scalacheck._
import play.api.libs.json.{ JsValue, Json }

trait Generators extends LambdaEventGenerators with JsValueGenerator
object Generators extends Generators

trait LambdaEventGenerators extends ApiGatewayEventGenerator
  with DynamoDbUpdateEventGenerator
  with ScheduledEventGenerator
  with KinesisEventGenerator
  with SNSEventGenerator

trait JsValueGenerator {
  val jsValueGen: Gen[JsValue] = for {
    name <- Gen.alphaStr
    age <- Gen.posNum[Int]
    luckyNumber <- Gen.chooseNum(Int.MinValue, Int.MaxValue)
  } yield Json.obj("name" -> name, "age" -> age, "luckyNumber" -> luckyNumber)
  implicit val jsValueArb: Arbitrary[JsValue] = Arbitrary(jsValueGen)
}

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

final case class DynamoDbUpdateEventGen(json: String, value: Int)
trait DynamoDbUpdateEventGenerator {
  def keysOnlyJson: String = {
    """
      |{
      |  "Records": [
      |    {
      |      "eventID": "0b5799c7352ae32f0f180d88a35fc2ec",
      |      "eventName": "INSERT",
      |      "eventVersion": "1.1",
      |      "eventSource": "aws:dynamodb",
      |      "awsRegion": "eu-west-1",
      |      "dynamodb": {
      |        "ApproximateCreationDateTime": 1.5142308E9,
      |        "Keys": {
      |          "id": {
      |            "S": "c922d4cb-0a98-42a3-bd8b-a87a5a4f30d4"
      |          }
      |        },
      |        "SequenceNumber": "100000000013566460532",
      |        "SizeBytes": 38,
      |        "StreamViewType": "KEYS_ONLY"
      |      },
      |      "eventSourceARN": "arn:aws:dynamodb:eu-west-1:123456789:table/sam-dynamodb-streams-seed-dnvriend-people/stream/2017-12-25T19:37:13.703"
      |    }
      |  ]
      |}
    """.stripMargin
  }

  def newImageJson: String = {
    """
        |{
        |  "Records": [
        |    {
        |      "eventID": "0101fd68132522bfa1a6601bfe851d80",
        |      "eventName": "INSERT",
        |      "eventVersion": "1.1",
        |      "eventSource": "aws:dynamodb",
        |      "awsRegion": "eu-west-1",
        |      "dynamodb": {
        |        "ApproximateCreationDateTime": 1.51423224E9,
        |        "Keys": {
        |          "id": {
        |            "S": "a811a9ea-6062-4e16-b14d-6eb2603bc6e8"
        |          }
        |        },
        |        "NewImage": {
        |          "json": {
        |            "S": "{\"name\":\"foo\",\"id\":\"a811a9ea-6062-4e16-b14d-6eb2603bc6e8\"}"
        |          },
        |          "id": {
        |            "S": "a811a9ea-6062-4e16-b14d-6eb2603bc6e8"
        |          }
        |        },
        |        "SequenceNumber": "88100000000034022438926",
        |        "SizeBytes": 138,
        |        "StreamViewType": "NEW_IMAGE"
        |      },
        |      "eventSourceARN": "arn:aws:dynamodb:eu-west-1:123456789:table/sam-dynamodb-streams-seed-dnvriend-people/stream/2017-12-25T20:03:14.819"
        |    }
        |  ]
        |}
      """.stripMargin
  }

  def newAndOldImage: String = {
    """
      |{
      |  "Records": [
      |    {
      |      "eventID": "dbd193752212e027d02a859e211b705f",
      |      "eventName": "INSERT",
      |      "eventVersion": "1.1",
      |      "eventSource": "aws:dynamodb",
      |      "awsRegion": "eu-west-1",
      |      "dynamodb": {
      |        "ApproximateCreationDateTime": 1.51423314E9,
      |        "Keys": {
      |          "id": {
      |            "S": "3a2de85d-059e-4297-a8a6-ae183d2c555d"
      |          }
      |        },
      |        "NewImage": {
      |          "json": {
      |            "S": "{\"name\":\"foo\",\"id\":\"3a2de85d-059e-4297-a8a6-ae183d2c555d\"}"
      |          },
      |          "id": {
      |            "S": "3a2de85d-059e-4297-a8a6-ae183d2c555d"
      |          }
      |        },
      |        "SequenceNumber": "141400000000019449549361",
      |        "SizeBytes": 138,
      |        "StreamViewType": "NEW_AND_OLD_IMAGES"
      |      },
      |      "eventSourceARN": "arn:aws:dynamodb:eu-west-1:123456789:table/sam-dynamodb-streams-seed-dnvriend-people/stream/2017-12-25T20:17:55.951"
      |    }
      |  ]
      |}
    """.stripMargin
  }

  def dynamoDbUpdateEventJson(value: Int) = {
    s"""
      |{
      |  "Records": [
      |    {
      |      "eventID": "1",
      |      "eventVersion": "1.0",
      |      "dynamodb": {
      |        "Keys": {
      |          "Id": {
      |            "N": "$value"
      |          }
      |        },
      |        "NewImage": {
      |          "Message": {
      |            "S": "New item!"
      |          },
      |          "Id": {
      |            "N": "101"
      |          }
      |        },
      |        "StreamViewType": "NEW_AND_OLD_IMAGES",
      |        "SequenceNumber": "111",
      |        "SizeBytes": 26
      |      },
      |      "awsRegion": "us-west-2",
      |      "eventName": "INSERT",
      |      "eventSourceARN": "arn:aws:dynamodb:us-west-2:account-id:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899",
      |      "eventSource": "aws:dynamodb"
      |    },
      |    {
      |      "eventID": "2",
      |      "eventVersion": "1.0",
      |      "dynamodb": {
      |        "OldImage": {
      |          "Message": {
      |            "S": "New item!"
      |          },
      |          "Id": {
      |            "N": "101"
      |          }
      |        },
      |        "SequenceNumber": "222",
      |        "Keys": {
      |          "Id": {
      |            "N": "101"
      |          }
      |        },
      |        "SizeBytes": 59,
      |        "NewImage": {
      |          "Message": {
      |            "S": "This item has changed"
      |          },
      |          "Id": {
      |            "N": "101"
      |          }
      |        },
      |        "StreamViewType": "NEW_AND_OLD_IMAGES"
      |      },
      |      "awsRegion": "us-west-2",
      |      "eventName": "MODIFY",
      |      "eventSourceARN": "arn:aws:dynamodb:us-west-2:account-id:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899",
      |      "eventSource": "aws:dynamodb"
      |    },
      |    {
      |      "eventID": "3",
      |      "eventVersion": "1.0",
      |      "dynamodb": {
      |        "Keys": {
      |          "Id": {
      |            "N": "101"
      |          }
      |        },
      |        "SizeBytes": 38,
      |        "SequenceNumber": "333",
      |        "OldImage": {
      |          "Message": {
      |            "S": "This item has changed"
      |          },
      |          "Id": {
      |            "N": "101"
      |          }
      |        },
      |        "StreamViewType": "NEW_AND_OLD_IMAGES"
      |      },
      |      "awsRegion": "us-west-2",
      |      "eventName": "REMOVE",
      |      "eventSourceARN": "arn:aws:dynamodb:us-west-2:account-id:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899",
      |      "eventSource": "aws:dynamodb"
      |    }
      |  ]
      |}
    """.stripMargin
  }

  val dynamodbUpdateEventGen: Gen[DynamoDbUpdateEventGen] = for {
    value <- Gen.posNum[Int]
  } yield DynamoDbUpdateEventGen(dynamoDbUpdateEventJson(value), value)
  implicit val dynamodbUpdateEventArb: Arbitrary[DynamoDbUpdateEventGen] = Arbitrary(dynamodbUpdateEventGen)
}

final case class ApiGatewayEventGen(json: String, value: String)
trait ApiGatewayEventGenerator {
  def apiGatewayEventJson(value: String) = {
    s"""
       |{
       |  "body": "{\\"test\\":\\"$value\\"}",
       |  "resource": "/{proxy+}",
       |  "requestContext": {
       |    "resourceId": "123456",
       |    "apiId": "1234567890",
       |    "resourcePath": "/{proxy+}",
       |    "httpMethod": "POST",
       |    "requestId": "c6af9ac6-7b61-11e6-9a41-93e8deadbeef",
       |    "accountId": "123456789012",
       |    "identity": {
       |      "apiKey": null,
       |      "userArn": null,
       |      "cognitoAuthenticationType": null,
       |      "caller": null,
       |      "userAgent": "Custom User Agent String",
       |      "user": null,
       |      "cognitoIdentityPoolId": null,
       |      "cognitoIdentityId": null,
       |      "cognitoAuthenticationProvider": null,
       |      "sourceIp": "127.0.0.1",
       |      "accountId": null
       |    },
       |    "stage": "prod"
       |  },
       |  "queryStringParameters": {
       |    "foo": "bar"
       |  },
       |  "headers": {
       |    "Via": "1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)",
       |    "Accept-Language": "en-US,en;q=0.8",
       |    "CloudFront-Is-Desktop-Viewer": "true",
       |    "CloudFront-Is-SmartTV-Viewer": "false",
       |    "CloudFront-Is-Mobile-Viewer": "false",
       |    "X-Forwarded-For": "127.0.0.1, 127.0.0.2",
       |    "CloudFront-Viewer-Country": "US",
       |    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
       |    "Upgrade-Insecure-Requests": "1",
       |    "X-Forwarded-Port": "443",
       |    "Host": "1234567890.execute-api.us-east-1.amazonaws.com",
       |    "X-Forwarded-Proto": "https",
       |    "X-Amz-Cf-Id": "cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==",
       |    "CloudFront-Is-Tablet-Viewer": "false",
       |    "Cache-Control": "max-age=0",
       |    "User-Agent": "Custom User Agent String",
       |    "CloudFront-Forwarded-Proto": "https",
       |    "Accept-Encoding": "gzip, deflate, sdch"
       |  },
       |  "pathParameters": {
       |    "proxy": "path/to/resource"
       |  },
       |  "httpMethod": "POST",
       |  "stageVariables": {
       |    "baz": "qux"
       |  },
       |  "path": "/path/to/resource"
       |}
    """.stripMargin
  }
  val apiGatewayEventGen: Gen[ApiGatewayEventGen] = for {
    value <- Gen.alphaStr
  } yield ApiGatewayEventGen(apiGatewayEventJson(value), value)
  implicit val apiGatewayEventArb: Arbitrary[ApiGatewayEventGen] = Arbitrary(apiGatewayEventGen)
}

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