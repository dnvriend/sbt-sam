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
    Subject: Option[String]
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
) {
  /**
   * Returns the message
   */
  def message: String = Sns.Message

  /**
   * Assumes the message is base64 encoded string
   */
  def messageAsBytes: Array[Byte] = java.util.Base64.getDecoder.decode(message)

  /**
   * Assumes the message is an unescaped JSON string
   */
  def messageAs[A: Reads]: A = Json.parse(message).as[A]

  /**
   * Assumes the message is an escaped JSON string
   */
  def messageEscapedAs[A](implicit reads: Reads[A]): A = {
    val escapedReads: Reads[A] = Reads.StringReads.map(Json.parse).andThen(reads)
    Json.parse(message).as[A](escapedReads)
  }
}