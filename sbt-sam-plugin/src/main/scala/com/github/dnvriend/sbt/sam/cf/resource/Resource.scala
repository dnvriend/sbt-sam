package com.github.dnvriend.sbt.sam.cf.resource

import com.github.dnvriend.sbt.sam.cf.resource.apigw.ServerlessApi
import com.github.dnvriend.sbt.sam.cf.resource.codebuild.CFCBProject
import com.github.dnvriend.sbt.sam.cf.resource.cognito.userpool.{ UserPool, UserPoolClient }
import com.github.dnvriend.sbt.sam.cf.resource.dynamodb.CFDynamoDBTable
import com.github.dnvriend.sbt.sam.cf.resource.firehose.s3.CFS3Firehose
import com.github.dnvriend.sbt.sam.cf.resource.iam.policy.CFIamRole
import com.github.dnvriend.sbt.sam.cf.resource.kinesis.CFKinesisStream
import com.github.dnvriend.sbt.sam.cf.resource.lambda.ServerlessFunction
import com.github.dnvriend.sbt.sam.cf.resource.s3.CFS3Bucket
import com.github.dnvriend.sbt.sam.cf.resource.sns.CFTopic
import play.api.libs.json.Writes

object Resource {
  implicit val writes: Writes[Resource] = Writes.apply {
    case resource: CFS3Bucket         => CFS3Bucket.writes.writes(resource)
    case resource: CFKinesisStream    => CFKinesisStream.writes.writes(resource)
    case resource: CFS3Firehose       => CFS3Firehose.writes.writes(resource)
    case resource: CFTopic            => CFTopic.writes.writes(resource)
    case resource: CFDynamoDBTable    => CFDynamoDBTable.writes.writes(resource)
    case resource: ServerlessApi      => ServerlessApi.writes.writes(resource)
    case resource: ServerlessFunction => ServerlessFunction.writes.writes(resource)
    case resource: CFIamRole          => CFIamRole.writes.writes(resource)
    case resource: CFCBProject        => CFCBProject.writes.writes(resource)
    case resource: UserPool           => UserPool.writes.writes(resource)
    case resource: UserPoolClient     => UserPoolClient.writes.writes(resource)
  }
}
trait Resource
