package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model._

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

  /**
   * Creates a backup for an existing table.
   * Each time you create an On-Demand Backup, the entire table data is backed up.
   * There is no limit to the number of on-demand backups that can be taken.
   */
  def backupTable(tableName: String, backupName: String, client: AmazonDynamoDB): Option[CreateBackupResult] = {
    Try(client.createBackup(
      new CreateBackupRequest()
        .withTableName(tableName)
        .withBackupName(backupName)
    )).toOption
  }

  /**
   * List backups associated with AWS account and table name
   */
  def listBackups(tableName: String, client: AmazonDynamoDB): Option[ListBackupsResult] = {
    Try(client.listBackups(new ListBackupsRequest().withTableName(tableName))).toOption
  }

  /**
   * Describes an existing backup of a table.
   */
  def describeBackup(backupArn: String, client: AmazonDynamoDB): Option[DescribeBackupResult] = {
    Try(client.describeBackup(new DescribeBackupRequest().withBackupArn(backupArn))).toOption
  }

  /**
   * Creates a new table from an existing backup. Any number of users can execute up to 10
   * concurrent restores in a given account.
   */
  def restoreTableFromBackup(backupArn: String, targetTableName: String, client: AmazonDynamoDB): Try[RestoreTableFromBackupResult] = {
    Try(client.restoreTableFromBackup(new RestoreTableFromBackupRequest()
      .withBackupArn(backupArn)
      .withTargetTableName(targetTableName)
    ))
  }
}
