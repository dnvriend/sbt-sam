package com.github.dnvriend.sbt.sam.cf.resource

import com.github.dnvriend.sbt.sam.cf.resource.apigw.ServerlessApi
import com.github.dnvriend.sbt.sam.cf.resource.dynamodb.CFDynamoDBTable
import com.github.dnvriend.sbt.sam.cf.resource.kinesis.CFKinesisStream
import com.github.dnvriend.sbt.sam.cf.resource.lambda.ServerlessFunction
import com.github.dnvriend.sbt.sam.cf.resource.s3.S3Bucket
import com.github.dnvriend.sbt.sam.cf.resource.sns.CFTopic
import play.api.libs.json.Writes

object Resource {
  implicit val writes: Writes[Resource] = Writes.apply {
    case resource: S3Bucket           => S3Bucket.writes.writes(resource)
    case resource: CFKinesisStream    => CFKinesisStream.writes.writes(resource)
    case resource: CFTopic            => CFTopic.writes.writes(resource)
    case resource: CFDynamoDBTable    => CFDynamoDBTable.writes.writes(resource)
    case resource: ServerlessApi      => ServerlessApi.writes.writes(resource)
    case resource: ServerlessFunction => ServerlessFunction.writes.writes(resource)
  }
}
trait Resource
