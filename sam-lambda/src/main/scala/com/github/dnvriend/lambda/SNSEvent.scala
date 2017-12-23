package com.github.dnvriend.lambda

import play.api.libs.json.{ Json, Reads }

import scala.tools.nsc.interpreter.InputStream

object SNS {
  implicit val reads: Reads[SNS] = Json.reads
}
case class SNS(
    SignatureVersion: String,
    Timestamp: String,
    Signature: String,
    SigningCertUrl: String,
    MessageId: String,
    Message: String,
    MessageAttributes: Map[String, Map[String, String]],
    Type: String,
    UnsubscribeUrl: String,
    TopicArn: String,
    Subject: String
)

object SNSEvent {
  implicit val reads: Reads[SNSEvent] = Json.reads
  def parse(input: InputStream): List[SNSEvent] = {
    (Json.parse(input) \ "Records").as[List[SNSEvent]]
  }
}
case class SNSEvent(
    EventVersion: String,
    EventSubscriptionArn: String,
    EventSource: String,
    Sns: SNS
)
/**
 * {
 * "Records": [
 * {
 * "EventVersion": "1.0",
 * "EventSubscriptionArn": "arn:aws:sns:EXAMPLE",
 * "EventSource": "aws:sns",
 * "Sns": {
 * "SignatureVersion": "1",
 * "Timestamp": "1970-01-01T00:00:00.000Z",
 * "Signature": "EXAMPLE",
 * "SigningCertUrl": "EXAMPLE",
 * "MessageId": "95df01b4-ee98-5cb9-9903-4c221d41eb5e",
 * "Message": "Hello from SNS!",
 * "MessageAttributes": {
 * "Test": {
 * "Type": "String",
 * "Value": "TestString"
 * },
 * "TestBinary": {
 * "Type": "Binary",
 * "Value": "TestBinary"
 * }
 * },
 * "Type": "Notification",
 * "UnsubscribeUrl": "EXAMPLE",
 * "TopicArn": "arn:aws:sns:EXAMPLE",
 * "Subject": "TestInvoke"
 * }
 * }
 * ]
 * }
 */
