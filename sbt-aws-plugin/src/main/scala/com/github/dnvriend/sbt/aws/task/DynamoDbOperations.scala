package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.TableDescription
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * batch-get-item                           | batch-write-item
 * create-table                             | delete-item
 * delete-table                             | describe-limits
 * describe-table                           | describe-time-to-live
 * get-item                                 | list-tables
 * list-tags-of-resource                    | put-item
 * query                                    | scan
 * tag-resource                             | untag-resource
 * update-item                              | update-table
 * update-time-to-live                      | wait
 */
object DynamoDbOperations {
  def client(): AmazonDynamoDB = {
    AmazonDynamoDBClientBuilder.defaultClient()
  }

  /**
   * Returns information about the table, including the current status of the table, when it was created,
   * the primary key schema, and any indexes on the table.
   */
  def describeTable(tableName: String, client: AmazonDynamoDB): Option[TableDescription] = {
    Try(client.describeTable(tableName).getTable).toOption
  }
}
