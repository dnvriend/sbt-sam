package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.dynamodbv2._
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._

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
  def client(cr: CredentialsAndRegion): AmazonDynamoDB = {
    AmazonDynamoDBClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

}
