package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cloudwatch._
import com.amazonaws.services.logs.model._
import com.amazonaws.services.logs.{ AWSLogs, AWSLogsClientBuilder, model }

import scala.collection.JavaConverters._
import scala.util.Try

//associate-kms-key                        | cancel-export-task
//create-export-task                       | create-log-group
//create-log-stream                        | delete-destination
//delete-log-group                         | delete-log-stream
//delete-metric-filter                     | delete-resource-policy
//delete-retention-policy                  | delete-subscription-filter
//describe-destinations                    | describe-export-tasks
//describe-log-groups                      | describe-log-streams
//describe-metric-filters                  | describe-resource-policies
//describe-subscription-filters            | disassociate-kms-key
//filter-log-events                        | get-log-events
//list-tags-log-group                      | put-destination
//put-destination-policy                   | put-log-events
//put-metric-filter                        | put-resource-policy
//put-retention-policy                     | put-subscription-filter
//tag-log-group                            | test-metric-filter
//untag-log-group                          | help

final case class LogEvent(
    // The time the event occurred, expressed as the number of milliseconds after Jan 1, 1970 00:00:00 UTC.
    timestamp: Long,
    // The time the event was ingested, expressed as the number of milliseconds after Jan 1, 1970 00:00:00 UTC.
    ingestionTime: Long,
    // The data contained in the log event.
    message: String)

/**
 *  You  can  use Amazon CloudWatch Logs to monitor, store, and access your
 *  log files from Amazon EC2 instances, AWS CloudTrail, or other  sources.
 */
object CloudWatchLogsOperations {
  def client(): AWSLogs = {
    AWSLogsClientBuilder.defaultClient()
  }

  /**
   * Lists the specified log groups. You can list all your log groups or filter the results by prefix.
   * The results are ASCII-sorted by log group name.
   */
  def describeLogGroups(client: AWSLogs): List[LogGroup] = {
    client.describeLogGroups().getLogGroups.asScala.toList
  }

  def findLogGroup(functionArn: String, client: AWSLogs): Option[LogGroup] = {
    val logGroupStartsWith: String = "/aws/lambda/" + functionArn.split(":").lastOption.getOrElse("")
    describeLogGroups(client).find(_.getLogGroupName.startsWith(logGroupStartsWith))
  }

  /**
   * Lists the log streams for the specified log group.
   */
  def describeLogStreams(logGroupName: String, client: AWSLogs): List[LogStream] = {
    Try(client.describeLogStreams(
      new DescribeLogStreamsRequest()
        .withLogGroupName(logGroupName)
    ).getLogStreams.asScala.toList).getOrElse(Nil)
  }

  /**
   * Lists log events from the specified log stream. You can list all the log events or filter using a time range.
   */
  def getLogEvents(logGroupName: String, startTime: Long, endTime: Long, client: AWSLogs): List[LogEvent] = {
    describeLogStreams(logGroupName, client).headOption.map { stream =>
      Try(client.getLogEvents(
        new GetLogEventsRequest()
          .withLogGroupName(logGroupName)
          .withLimit(100)
          .withLogStreamName(stream.getLogStreamName)
          .withStartFromHead(true)
      ).getEvents.asScala.toList).getOrElse(List.empty)
        .map(event => LogEvent(event.getTimestamp, event.getIngestionTime, event.getMessage))
        .sortBy(_.timestamp).reverse
    }.getOrElse(Nil)
  }
}
