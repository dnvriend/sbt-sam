package com.github.dnvriend.repo.dynamodb

import java.util.UUID

import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBClientBuilder }
import com.github.dnvriend.lambda.SamContext
import play.api.libs.json.{ Json, Reads, Writes }

import scala.collection.JavaConverters._
import scalaz.Scalaz._
import scalaz._

object DynamoDBJsonWithRangeKeyRepository {
  def apply(
    tableName: String,
    ctx: SamContext,
    idAttributeName: String = "id",
    rangeKeyAttributeName: String = "range",
    payloadAttributeName: String = "json"): DynamoDBJsonWithRangeKeyRepository = {
    new DynamoDBJsonWithRangeKeyRepository(tableName, ctx, idAttributeName, rangeKeyAttributeName, payloadAttributeName)
  }
}

/**
 * DynamoDBJsonWithRangeKeyRepository is a repository with three attributes,
 * an 'id', 'range' and 'json'. It stores a payload as JSON string
 * in the 'json' attribute. Values are stored by 'id' and 'range' key.
 * A list of values can be retrieved for a certain 'id'.
 *
 * The attribute names of 'id', 'range' and 'json' are configurable.
 */
class DynamoDBJsonWithRangeKeyRepository(
    tableName: String,
    ctx: SamContext,
    idAttributeName: String = "id",
    rangeKeyAttributeName: String = "range",
    payloadAttributeName: String = "json") {
  val table: String = ctx.dynamoDbTableName(tableName)
  val db: AmazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient()

  /**
   * Returns a random UUID as String
   */
  def id(): String = UUID.randomUUID.toString

  /**
   * Marshal value 'A' to Json String
   */
  private def marshal[A: Writes](value: A): String = {
    Json.toJson(value).toString
  }

  /**
   * Unmarshal Json String to value 'A'
   */
  private def unmarshal[A: Reads](json: String): A = {
    Json.parse(json).as[A]
  }

  /**
   * Stores a value with keys 'id' and 'range'
   */
  def put[A: Writes](id: String, range: String, value: A): Disjunction[Throwable, PutItemResult] = {
    Disjunction.fromTryCatchNonFatal {
      db.putItem(
        new PutItemRequest()
          .withTableName(table)
          .withReturnValues(ReturnValue.NONE)
          .withItem(
            Map(
              idAttributeName -> new AttributeValue(id),
              rangeKeyAttributeName -> new AttributeValue(range),
              payloadAttributeName -> new AttributeValue(marshal(value))
            ).asJava
          )
      )
    }
  }

  /**
   * Returns a value, if present with keys 'id' and 'range'
   */
  def find[A: Reads](id: String, range: String): Disjunction[String, A] = for {
    attributes <- Disjunction.fromTryCatchNonFatal(db.getItem(table, Map(idAttributeName -> new AttributeValue(id), rangeKeyAttributeName -> new AttributeValue(range)).asJava).getItem.asScala).leftMap(_.getMessage)
    jsonString <- Option(attributes).flatMap(_.get(payloadAttributeName)).map(_.getS).toSuccess(s"No '$payloadAttributeName' attribute in table").disjunction
    value <- Disjunction.fromTryCatchNonFatal(Json.parse(jsonString).as[A]).leftMap(_.getMessage)
  } yield value

  /**
   * Returns the id, range and payload as tuple, in that order
   */
  def find[A: Reads](id: String, limit: Int = 100): Disjunction[Throwable, List[(String, String, A)]] = Disjunction.fromTryCatchNonFatal {
    def mapValues(id: AttributeValue, range: AttributeValue, payload: AttributeValue): (String, String, A) = {
      (id.getS, range.getS, Json.parse(payload.getS).as[A])
    }
    def mapAttributes(attr: Map[String, AttributeValue]): Option[(String, String, A)] = {
      (attr.get(idAttributeName) |@| attr.get(rangeKeyAttributeName) |@| attr.get(payloadAttributeName))(mapValues)
    }
    db.query(new QueryRequest(table)
      .withKeyConditionExpression(s"$idAttributeName = :id")
      .withExpressionAttributeValues(Map(":id" -> new AttributeValue(id)).asJava)
      .withSelect(Select.ALL_ATTRIBUTES)
      .withLimit(limit)
    ).getItems.asScala.toList.map(_.asScala.toMap).flatMap(mapAttributes)
  }

  /**
   * Updates a value with keys 'id' and 'range'
   */
  def update[A: Writes](id: String, range: String, value: A): Disjunction[Throwable, UpdateItemResult] = {
    Disjunction.fromTryCatchNonFatal {
      db.updateItem(table, Map(idAttributeName -> new AttributeValue(id)).asJava, Map(payloadAttributeName -> new AttributeValueUpdate(new AttributeValue(marshal(value)), AttributeAction.PUT)).asJava)
    }
  }

  /**
   * Deletes a value with key 'id'
   */
  def delete(id: String, range: String): Disjunction[Throwable, DeleteItemResult] = {
    Disjunction.fromTryCatchNonFatal {
      db.deleteItem(table, Map(
        idAttributeName -> new AttributeValue(id),
        rangeKeyAttributeName -> new AttributeValue(range)
      ).asJava)
    }
  }

  /**
   * Returns a list of values, default 100 items with id, range and payload order
   */
  def list[A: Reads](limit: Int = 100): Disjunction[Throwable, List[(String, String, A)]] = {
    Disjunction.fromTryCatchNonFatal {
      db.scan(new ScanRequest()
        .withTableName(table)
        .withAttributesToGet(idAttributeName, rangeKeyAttributeName, payloadAttributeName)
        .withLimit(limit)
      ).getItems.asScala.toList.map(m => (m.get(idAttributeName).getS, m.get(rangeKeyAttributeName).getS, m.get(payloadAttributeName).getS))
        .map({ case (id, range, json) => (id, range, Json.parse(json).as[A]) })
    }
  }
}
