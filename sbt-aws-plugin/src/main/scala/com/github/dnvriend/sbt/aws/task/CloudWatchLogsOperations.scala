package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.logs.model._
import com.amazonaws.services.logs.{ AWSLogs, AWSLogsClientBuilder }

import scala.collection.JavaConverters._
import scalaz.Scalaz._
import scalaz._

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
 * You  can  use Amazon CloudWatch Logs to monitor, store, and access your
 * log files from Amazon EC2 instances, AWS CloudTrail, or other  sources.
 */
object CloudWatchLogsOperations {
  def client(): AWSLogs = {
    AWSLogsClientBuilder.defaultClient()
  }

  /**
   * Lists the specified log groups. You can list all your log groups or filter the results by prefix.
   * The results are ASCII-sorted by log group name.
   */
  def describeLogGroups(logGroupNamePrefix: String, client: AWSLogs): List[LogGroup] = {
    val maxLimit: Int = 50
    Disjunction.fromTryCatchNonFatal {
      client.describeLogGroups(
        new DescribeLogGroupsRequest()
          .withLogGroupNamePrefix(logGroupNamePrefix)
          .withLimit(maxLimit)
      ).getLogGroups.asScala.toList
    }.valueOr(logOrNil[LogGroup])
  }

  def findLogGroup(functionName: String, client: AWSLogs): Option[LogGroup] = {
    val logGroupName: String = "/aws/lambda/" + functionName
    describeLogGroups(logGroupName, client)
      .find(_.getLogGroupName == logGroupName)
  }

  /**
   * Lists the log streams for the specified log group.
   */
  def describeLogStreams(logGroupName: String, client: AWSLogs): List[LogStream] = {
    Disjunction.fromTryCatchNonFatal {
      client.describeLogStreams(
        new DescribeLogStreamsRequest()
          .withLogGroupName(logGroupName)
      ).getLogStreams.asScala.toList
    }.valueOr(logOrNil[LogStream])
  }

  /**
   * Lists log events from the specified log stream. You can list all the log events or filter using a time range.
   */
  def getLogEvents(logGroupName: String, client: AWSLogs): List[LogEvent] = {
    val logStreams = describeLogStreams(logGroupName, client).sortBy(_.getLastEventTimestamp).reverse
    val logEvents: Disjunction[String, List[LogEvent]] = for {
      stream <- logStreams.headOption.toRightDisjunction(s"No logGroup (yet) for logGroupName: $logGroupName; please invoke the lambda to create one.")
      logEvents <- Disjunction.fromTryCatchNonFatal {
        client.getLogEvents(
          new GetLogEventsRequest()
            .withLogGroupName(logGroupName)
            .withLimit(100)
            .withLogStreamName(stream.getLogStreamName)
            .withStartFromHead(true)
        ).getEvents.asScala.toList
      }.leftMap(_.getMessage)
    } yield logEvents.map(event => LogEvent(event.getTimestamp, event.getIngestionTime, event.getMessage)).sortBy(_.timestamp).reverse
    logEvents.valueOr(logOrNil[LogEvent])
  }

  private def logOrNil[A](t: Throwable): List[A] = {
    logOrNil(t.getMessage)
  }
  private def logOrNil[A](msg: String): List[A] = {
    println(msg)
    Nil
  }
}
