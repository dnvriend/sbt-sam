package com.github.dnvriend.lambda.generators

import org.scalacheck._
import play.api.libs.json.{ JsValue, Json }

trait Generators extends LambdaEventGenerators with JsValueGenerator
object Generators extends Generators

trait LambdaEventGenerators extends ApiGatewayEventGenerator
  with DynamoDbUpdateEventGenerator

trait JsValueGenerator {
  val jsValueGen: Gen[JsValue] = for {
    name <- Gen.alphaStr
    age <- Gen.posNum[Int]
    luckyNumber <- Gen.chooseNum(Int.MinValue, Int.MaxValue)
  } yield Json.obj("name" -> name, "age" -> age, "luckyNumber" -> luckyNumber)
  implicit val jsValueArb: Arbitrary[JsValue] = Arbitrary(jsValueGen)
}

final case class DynamoDbUpdateEvent(json: String, value: Int)
trait DynamoDbUpdateEventGenerator {
  def dynamoDbUpdateEvent(value: Int) = {
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

  val dynamodbUpdateEventGen: Gen[DynamoDbUpdateEvent] = for {
    value <- Gen.posNum[Int]
  } yield DynamoDbUpdateEvent(dynamoDbUpdateEvent(value), value)
  implicit val dynamodbUpdateEventArb: Arbitrary[DynamoDbUpdateEvent] = Arbitrary(dynamodbUpdateEventGen)
}

final case class ApiGatewayEvent(json: String, value: String)
trait ApiGatewayEventGenerator {
  def apiGatewayEvent(value: String) = {
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
  val apiGatewayEventGen: Gen[ApiGatewayEvent] = for {
    value <- Gen.alphaStr
  } yield ApiGatewayEvent(apiGatewayEvent(value), value)
  implicit val apiGatewayEventArb: Arbitrary[ApiGatewayEvent] = Arbitrary(apiGatewayEventGen)
}
