package com.github.dnvriend.lambda

import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }

object SamContext {
  def projectName = sys.env.getOrElse("PROJECT_NAME", "")
  def stage = sys.env.getOrElse("STAGE", "")
  def region = sys.env.getOrElse("AWS_REGION", "")
  def accountId = sys.env.getOrElse("AWS_ACCOUNT_ID", "")
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
) {
  /**
   * Returns the Arn for an sns topic
   */
  def snsTopicArn(topicName: String): String = {
    s"arn:aws:sns:$region:$accountId:$projectName-$stage-$topicName"
  }

  /**
   * Returns the DynamoDB table name
   */
  def dynamoDbTableName(tableName: String): String = {
    s"$projectName-$stage-$tableName"
  }
}
