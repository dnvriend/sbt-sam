package com.github.dnvriend.repo.dynamodb

import java.nio.ByteBuffer
import java.util.UUID

import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBClientBuilder }
import com.github.dnvriend.lambda.SamContext
import com.github.dnvriend.repo.BinaryRepository

import scala.collection.JavaConverters._
import scalaz.Scalaz._
import scalaz._

/**
 * BinaryDynamoDBRepository is a repository with only two attributes,
 * an 'id' and 'blob' attribute. It stores a payload as Bytes
 * in the 'blob' attribute
 */
class DynamoDBBinaryRepository(tableName: String, ctx: SamContext) extends BinaryRepository {
  val table: String = ctx.dynamoDbTableName(tableName)
  val db: AmazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient()

  /**
   * Returns a random UUID as String
   */
  def id(): String = UUID.randomUUID.toString

  /**
   * Wraps a byte array into a buffer
   */
  private def wrapBytes(value: Array[Byte]): ByteBuffer = {
    ByteBuffer.wrap(value)
  }

  /**
   * Stores a value with key 'id'
   */
  override def put(id: String, value: Array[Byte]): Unit = {
    val result: String = Disjunction.fromTryCatchNonFatal {
      db.putItem(
        new PutItemRequest()
          .withTableName(table)
          .withReturnValues(ReturnValue.NONE)
          .withItem(
            Map(
              "id" -> new AttributeValue(id),
              "blob" -> new AttributeValue().withB(wrapBytes(value))
            ).asJava
          )
      )
    }.bimap(t => t.getMessage, result => result.toString).merge
    ctx.logger.log(result)
  }

  /**
   * Returns a value, if present with key 'id'
   */
  override def find(id: String): Option[Array[Byte]] = {
    val result: Disjunction[String, Array[Byte]] = for {
      attributes <- Disjunction.fromTryCatchNonFatal(db.getItem(table, Map("id" -> new AttributeValue(id)).asJava).getItem.asScala).leftMap(_.getMessage)
      blobField <- Validation.lift(attributes)(attr => attr.get("blob").isDefined, "No blob attribute in table").map(_.get("blob")).disjunction
      byteBuff <- blobField.toRightDisjunction("No blob attribute in table").map(_.getB)
    } yield byteBuff.array()

    if (result.isLeft) {
      val message: String = result.swap.getOrElse(s"Error while finding value for key '$id'")
      ctx.logger.log(message)
    }

    result.toOption
  }

  /**
   * Updates a value with key 'id'
   */
  override def update(id: String, value: Array[Byte]): Unit = {
    val result: String = Disjunction.fromTryCatchNonFatal {
      db.updateItem(table, Map("id" -> new AttributeValue(id)).asJava, Map("blob" -> new AttributeValueUpdate(new AttributeValue().withB(wrapBytes(value)), AttributeAction.PUT)).asJava)
    }.bimap(t => t.getMessage, result => result.toString).merge
    ctx.logger.log(result)
  }

  /**
   * Deletes a value with key 'id'
   */
  override def delete(id: String): Unit = {
    val result: String = Disjunction.fromTryCatchNonFatal {
      db.deleteItem(table, Map("id" -> new AttributeValue(id)).asJava)
    }.bimap(t => t.getMessage, result => result.toString).merge
    ctx.logger.log(result)
  }

  /**
   * Returns a list of values, default 100 items
   */
  override def list(limit: Int = 100): List[(String, Array[Byte])] = {
    Disjunction.fromTryCatchNonFatal {
      db.scan(new ScanRequest()
        .withTableName(table)
        .withAttributesToGet("id", "blob")
        .withLimit(limit)
      ).getItems.asScala.map(m => (m.get("id").getS, m.get("blob").getB.array()))
        .toList
    }.valueOr { error =>
      ctx.logger.log(error.getMessage)
      List.empty[(String, Array[Byte])]
    }
  }
}
