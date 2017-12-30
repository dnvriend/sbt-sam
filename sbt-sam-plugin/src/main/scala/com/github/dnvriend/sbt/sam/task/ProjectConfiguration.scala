package com.github.dnvriend.sbt.sam.task

import com.amazonaws.regions.Regions
import com.github.dnvriend.ops.AnyOps
import com.github.dnvriend.sbt.aws.domain.IAMDomain.CredentialsRegionAndUser
import com.github.dnvriend.sbt.aws.task.{AmazonUser, Arn}
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.github.dnvriend.sbt.sam.resource.dynamodb.model._
import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import com.github.dnvriend.sbt.sam.resource.kinesis.model._
import com.github.dnvriend.sbt.sam.resource.sns.model._
import com.github.dnvriend.sbt.sam.resource.policy.model._

import scala.util.matching.Regex

case class SamCFTemplateName(value: String) {
  val templateNameRegex: Regex = """[a-zA-Z][-a-zA-Z0-9]*""".r
  require(templateNameRegex.findFirstIn(value).isDefined, s"CloudFormation template name, with name '$value', must satisfy regular expression pattern: [a-zA-Z][-a-zA-Z0-9]*")
}

object SamS3BucketName {
  val bucketNameRegex: Regex = """^([a-z]|(\d(?!\d{0,2}\.\d{1,3}\.\d{1,3}\.\d{1,3})))([a-z\d]|(\.(?!(\.|-)))|(-(?!\.))){1,61}[a-z\d\.]$""".r
}
case class SamS3BucketName(value: String) {
  require(SamS3BucketName.bucketNameRegex.findFirstIn(value).exists(name => name.length > 3 && name.length < 63), s"Bucket name with name '$value', should be between 3 and 63 characters long")
}
case class SamResourcePrefixName(value: String)
case class SamStage(value: String) {
  require(!List("-", ".", " ", "/").exists(char => value.contains(char)), s"sam stage with value '$value', should not contain '.', '-', '/' or spaces")
}
case class SamResources(lambdas: Set[LambdaHandler],
                        tables: Set[TableWithIndex],
                        policies: Set[Policy],
                        topics: Set[Topic],
                        streams: Set[KinesisStream],
                        buckets: Set[S3Bucket],
                        s3Firehoses: Set[S3Firehose],
                       )
object ProjectConfiguration {
  def fromConfig(
    projectName: String,
    projectVersion: String,
    samS3BucketName: String,
    samCFTemplateName: String,
    samResourcePrefixName: String,
    samStage: String,
    credentialsRegionAndUser: CredentialsRegionAndUser,
    amazonUser: AmazonUser,
    samResources: SamResources,
    ): ProjectConfiguration = {
    val cfTemplateName = samCFTemplateName.replace(".", "-").replace(" ", "")
    val s3BucketName = samS3BucketName.replace(".", "-").replace(" ", "")
    ProjectConfiguration(
      projectName,
      projectVersion,
      SamS3BucketName(s3BucketName),
      SamCFTemplateName(cfTemplateName),
      SamStage(samStage),
      SamResourcePrefixName(samResourcePrefixName),
      credentialsRegionAndUser,
      amazonUser,
      samResources.lambdas.toList,
      samResources.tables.toList,
      samResources.policies.toList,
      samResources.topics.toList,
      samResources.streams.toList,
      samResources.buckets.toList,
      samResources.s3Firehoses.toList,
    )
  }
}
case class ProjectConfiguration(
    projectName: String,
    projectVersion: String,
    samS3BucketName: SamS3BucketName,
    samCFTemplateName: SamCFTemplateName,
    samStage: SamStage,
    samResourcePrefixName: SamResourcePrefixName,
    credentialsRegionAndUser: CredentialsRegionAndUser,
    amazonUser: AmazonUser,
    lambdas: List[LambdaHandler] = List.empty,
    tables: List[TableWithIndex] = List.empty,
    policies: List[Policy] = List.empty,
    topics: List[Topic] = List.empty,
    streams: List[KinesisStream] = List.empty,
    buckets: List[S3Bucket] = List.empty,
    s3Firehoses: List[S3Firehose] = List.empty,
) extends AnyOps {
  def httpHandlers: List[HttpHandler] = lambdas.collect({case h: HttpHandler => h})
  def existHttpHandlers: Boolean = httpHandlers.nonEmpty
  def scheduledEventHandlers: List[ScheduledEventHandler] = lambdas.collect({case h: ScheduledEventHandler => h})
  def snsEventHandlers: List[SNSEventHandler] = lambdas.collect({case h: SNSEventHandler => h})
  def dynamoHandlers: List[DynamoHandler] = lambdas.collect({case h: DynamoHandler => h})
  def kinesisEventHandlers: List[KinesisEventHandler] = lambdas.collect({case h: KinesisEventHandler => h})
  def userArn: Arn = Arn.fromArnString(credentialsRegionAndUser.user.getArn.wrap[Arn])
  def getRegion: Regions = credentialsRegionAndUser.credentialsAndRegion.region
}