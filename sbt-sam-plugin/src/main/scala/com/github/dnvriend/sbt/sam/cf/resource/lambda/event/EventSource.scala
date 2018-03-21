package com.github.dnvriend.sbt.sam.cf.resource.lambda.event

import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.apigw.ApiGatewayEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.cloudwatch.CloudWatchEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.dynamodb.DynamoDBEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.kinesis.KinesisEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.s3.S3EventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.schedule.ScheduledEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.sns.SnsEventSource
import play.api.libs.json.Writes

object EventSource {
  implicit val writes: Writes[EventSource] = Writes.apply {
    case event: ApiGatewayEventSource => ApiGatewayEventSource.writes.writes(event)
    case event: SnsEventSource        => SnsEventSource.writes.writes(event)
    case event: ScheduledEventSource  => ScheduledEventSource.writes.writes(event)
    case event: S3EventSource         => S3EventSource.writes.writes(event)
    case event: KinesisEventSource    => KinesisEventSource.writes.writes(event)
    case event: DynamoDBEventSource   => DynamoDBEventSource.writes.writes(event)
    case event: CloudWatchEventSource => CloudWatchEventSource.writes.writes(event)
    case event: NoEventSource         => NoEventSource.writes.writes(event)
  }
}

trait EventSource

