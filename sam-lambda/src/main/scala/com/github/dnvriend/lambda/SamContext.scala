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
    s"arn:aws:sns:$region:$accountId:${determineName(topicName)}"
  }

  /**
   * Returns the Arn for a kinesis stream
   */
  def kinesisStreamArn(streamName: String): String = {
    s"arn:aws:kinesis:$region:$accountId:stream/${determineName(streamName)}"
  }

  /**
   * Determines whether a resource name is imported, if so
   * if returns the name to use for an imported resource, else
   * it will determine the name to use for a local resource
   * // stage - projectName - name
   */
  def determineName(name: String): String = {
    if (name.startsWith("import")) {
      val parts = name.split(":")
      val projectName = parts.drop(1).head
      val nameToImport = parts.drop(2).head
      s"$stage-$projectName-$nameToImport"
    } else {
      s"$stage-$projectName-$name"
    }
  }

  /**
   * Returns the scoped Kinesis stream name
   */
  def kinesisStreamName(streamName: String): String = {
    determineName(streamName)
  }

  /**
   * Returns the scoped SNS Topic name
   */
  def snsTopicName(topicName: String): String = {
    determineName(topicName)
  }

  /**
   * Returns the scoped DynamoDB table name
   */
  def dynamoDbTableName(tableName: String): String = {
    determineName(tableName)
  }
}