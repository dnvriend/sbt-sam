package com.github.dnvriend.lambda.generators

import org.scalacheck.{ Arbitrary, Gen }

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

