package com.github.dnvriend.repo.dynamodb

import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, ReturnValue, UpdateItemRequest }
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBClientBuilder }
import com.github.dnvriend.lambda.SamContext

import scala.collection.JavaConverters._

object DynamoDBCounterRepository {
  def apply(
    tableName: String,
    ctx: SamContext,
    counterKeyName: String = "counter_key",
    counterValueName: String = "counter_value"): DynamoDBCounterRepository = {
    new DynamoDBCounterRepository(tableName, ctx, counterKeyName, counterValueName)
  }
}

/**
 * DynamoDBCounterRepository is a repository with only two attributes,
 * an 'counter_key' and 'counter_key' attribute. It supports only a single
 * operation 'incrementAndGet', that increments a counter by '1' and returns
 * the current value. The counter starts from 0, so the first returned value is '1', etc.
 * The name of the columns can be configured. The repo supports multiple counters, each
 * counter has its own name and state.
 */
class DynamoDBCounterRepository(
    tableName: String,
    ctx: SamContext,
    counterKeyName: String = "counter_key",
    counterValueName: String = "counter_value") {
  val table: String = ctx.dynamoDbTableName(tableName)
  val db: AmazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient()

  def incrementAndGet(counterKey: String, incrementBy: Int = 1): Option[Long] = {
    db.updateItem(
      new UpdateItemRequest()
        .withTableName(table)
        .withKey(Map(counterKeyName -> new AttributeValue(counterKey)).asJava)
        .withUpdateExpression("ADD counter_value :incr")
        .withExpressionAttributeValues(Map(":incr" -> new AttributeValue().withN(incrementBy.toString)).asJava)
        .withReturnValues(ReturnValue.UPDATED_NEW)
    ).getAttributes.asScala.get("counter_value").map(_.getN.toLong)
  }
}
