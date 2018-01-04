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