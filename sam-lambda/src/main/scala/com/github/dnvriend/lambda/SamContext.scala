package com.github.dnvriend.lambda

import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }

object SamContext {
  def projectName: String = sys.env.getOrElse("PROJECT_NAME", "")
  def stage: String = sys.env.getOrElse("STAGE", "")
  def region: String = sys.env.getOrElse("AWS_REGION", "")
  def accountId: String = sys.env.getOrElse("AWS_ACCOUNT_ID", "")
  def apply(context: Context): SamContext = {
    SamContext(
      context,
      context.getLogger,
      projectName,
      stage,
      region,
      accountId
    )
  }
}

case class SamContext(
    underlying: Context,
    logger: LambdaLogger,
    projectName: String,
    stage: String,
    region: String,
    accountId: String
) extends {
  /**
   * Returns the Arn for an sns topic
   */
  def snsTopicArn(topicName: String): String = {
    s"arn:aws:sns:$region:$accountId:$projectName-$stage-$topicName"
  }

  /**
   * Returns the Arn for a kinesis stream
   */
  def kinesisStreamArn(streamName: String): String = {
    s"arn:aws:kinesis:$region:$accountId:stream/$projectName-$stage-$streamName"
  }

  /**
   * Returns the scoped Kinesis stream name
   */
  def kinesisStreamName(streamName: String): String = {
    s"$projectName-$stage-$streamName"
  }

  /**
   * Returns the scoped DynamoDB table name
   */
  def dynamoDbTableName(tableName: String): String = {
    val name: String = if (tableName.startsWith("import")) {
      val parts = tableName.split(":")
      val exportComponentName = parts.drop(1).head
      val tableNameToImport = parts.drop(2).head
      s"$exportComponentName-$stage-$tableNameToImport"
    } else {
      s"$projectName-$stage-$tableName"
    }
    logger.log(s"Returning table name: $name")
    name
  }
}