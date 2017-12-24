package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{ Stack, StackResource }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.sns.AmazonSNS
import com.github.dnvriend.sbt.aws.task.{ DynamoDbOperations, SNSOperations, SamStack }
import sbt.util.Logger

import scalaz.Show

object CloudFormationStackResource {
  implicit val show: Show[CloudFormationStackResource] = Show.shows(m => {
    s"${m.logicalResourceId}- ${m.resourceType}- ${m.resourceStatus}- ${m.timestamp}"
  })
  def fromStackResource(resource: StackResource): CloudFormationStackResource = {
    CloudFormationStackResource(
      resource.getLogicalResourceId,
      resource.getResourceType,
      resource.getResourceStatus,
      resource.getTimestamp.toString
    )
  }
}
case class CloudFormationStackResource(
    logicalResourceId: String,
    resourceType: String,
    resourceStatus: String,
    timestamp: String)

object CloudFormationStackInfo {
  def run(
    config: ProjectConfiguration,
    stack: Option[Stack],
    client: AmazonCloudFormation,
    dynamoClient: AmazonDynamoDB,
    snsClient: AmazonSNS,
    log: Logger
  ): Unit = {

    val projectName: String = config.projectName
    val stage: String = config.samStage.value

    log.info("Stack details:")
    val samStack = stack.map(SamStack.fromStack)
    log.info(samStack.map(SamStack.show.shows).getOrElse("No stack details"))
    log.info("DynamoDbTables:")
    config.tables.map(table => {
      val tableName = s"$projectName-$stage-${table.name}"
      (table, DynamoDbOperations.describeTable(tableName, dynamoClient))
    }).foreach {
      case (table, optionalInfo) =>
        log.info(s"* ${table.name} -> ${optionalInfo.fold(Console.YELLOW + "not yet deployed")(info => Console.GREEN + s"${info.getTableArn}")}")
    }
    log.info("SNS Topics:")
    config.topics.map { topic =>
      val topicName = s"$projectName-$stage-${topic.name}"
      (topic, SNSOperations.describeTopic(topicName, snsClient))
    }.foreach {
      case (topic, optionalInfo) =>
        log.info(s"* ${topic.name} -> ${optionalInfo.fold(Console.YELLOW + "not yet deployed")(info => Console.GREEN + info.getTopicArn)}")
    }
    log.info("Kinesis Streams:")
    log.info("Endpoints:")
    samStack.flatMap(_.serviceEndpoint).foreach { endpoint =>
      config.lambdas.foreach {
        case HttpHandler(_, HttpConf(path, method, auth)) =>
          log.info(Console.GREEN + s"${method.toUpperCase} - ${endpoint.value}$path")
        case _ =>
      }
    }
  }
}
