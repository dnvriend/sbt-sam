package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{ Stack, StackResource }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.sns.AmazonSNS
import com.github.dnvriend.sbt.aws.task.{ DynamoDbOperations, KinesisOperations, SNSOperations, SamStack }
import sbt.util.Logger

import scalaz._
import scalaz.Scalaz._

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
    kinesisClient: AmazonKinesis,
    log: Logger
  ): Unit = {

    val projectName: String = config.projectName
    val stage: String = config.samStage.value
    val samStack = stack.map(SamStack.fromStack)
    val stackStatus: String = {
      samStack.map(SamStack.show.shows)
        .getOrElse(Console.YELLOW + s"Stack '${config.samCFTemplateName.value}' is not yet deployed")
    }

    val kinesisStreamsSummary: String = {
      config.streams.map { stream =>
        val streamName = s"$projectName-$stage-${stream.name}"
        (stream, KinesisOperations.describeStream(streamName, kinesisClient))
      }.map {
        case (topic, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed") { info =>
            Console.GREEN + info.getStreamARN
          }
          val retensionPeriodHours = topic.retensionPeriodHours
          s"* ${topic.name} - ($retensionPeriodHours hours) -> $info"
      }.toList.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No streams configured")
    }

    val snsTopicsSummary: String = {
      config.topics.map { topic =>
        val topicName = s"$projectName-$stage-${topic.name}"
        (topic, SNSOperations.describeTopic(topicName, snsClient))
      }.map {
        case (topic, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed") { info =>
            Console.GREEN + info.getTopicArn
          }
          s"* ${topic.name} -> $info"
      }.toList.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No topics configured")
    }

    val tablesSummary: String = {
      config.tables.map(table => {
        val tableName = s"$projectName-$stage-${table.name}"
        (table, DynamoDbOperations.describeTable(tableName, dynamoClient))
      }).map {
        case (table, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed") { info =>
            Console.GREEN + info.getTableArn
          }
          val streamName = table.stream.fold("")(stream => s"stream: $stream")
          s"* ${table.name} $streamName -> $info"
      }.toList.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No tables configured")
    }

    val endpointSummary: String = {
      samStack.flatMap(_.serviceEndpoint).map { endpoint =>
        config.lambdas.map {
          case HttpHandler(_, HttpConf(path, method, auth)) =>
            Console.GREEN + s"${method.toUpperCase} - ${endpoint.value}$path"
          case _ => ""
        }.toList.toNel.map(_.intercalate("\n")).getOrElse("No http handlers configured")
      }.getOrElse(Console.YELLOW + "No service endpoint found")
    }

    val report =
      s"""$stackStatus
        |DynamoDbTables:
        |$tablesSummary
        |SNS Topics:
        |$snsTopicsSummary
        |Kinesis Streams:
        |$kinesisStreamsSummary
        |Endpoints:
        |$endpointSummary
      """.stripMargin

    log.info(report)
  }
}