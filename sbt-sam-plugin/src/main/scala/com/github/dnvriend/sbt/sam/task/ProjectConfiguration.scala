package com.github.dnvriend.sbt.sam.task

import com.amazonaws.regions.Regions
import com.github.dnvriend.ops.AnyOps
import com.github.dnvriend.sbt.aws.domain.IAMDomain.CredentialsRegionAndUser
import com.github.dnvriend.sbt.aws.task.{AmazonUser, Arn}
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.github.dnvriend.sbt.sam.resource.cognito.model.{Authpool, ImportAuthPool}
import com.github.dnvriend.sbt.sam.resource.dynamodb.model._
import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import com.github.dnvriend.sbt.sam.resource.kinesis.model._
import com.github.dnvriend.sbt.sam.resource.role.model.IamRole
import com.github.dnvriend.sbt.sam.resource.sns.model._
import com.github.dnvriend.sbt.sam.task.ClassifySqlFiles.SqlApplication


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
case class SamResources(
                         authpool: Option[Authpool] = None,
                         importAuthPool: Option[ImportAuthPool]= None,
                         lambdas: Set[LambdaHandler] = Set.empty,
                         tables: Set[TableWithIndex] = Set.empty,
                         topics: Set[Topic] = Set.empty,
                         streams: Set[KinesisStream] = Set.empty,
                         buckets: Set[S3Bucket] = Set.empty,
                         s3Firehoses: Set[S3Firehose] = Set.empty,
                         iamRoles: Set[IamRole] = Set.empty,
                         sqlApplications: List[SqlApplication] = List.empty,
                        )

object ProjectConfiguration extends AnyOps {
  def fromConfig(
                  projectName: String,
                  projectVersion: String,
                  projectDescription: String,
                  deploymentBucketName: String,
                  samCFTemplateName: String,
                  samResourcePrefixName: String,
                  samStage: String,
                  credentialsRegionAndUser: CredentialsRegionAndUser,
                  amazonUser: AmazonUser,
                  samResources: SamResources,
    ): ProjectConfiguration = {
    val arn = Arn.fromArnString(credentialsRegionAndUser.user.getArn.wrap[Arn])
    val accountId = arn.accountId.value
    val region = credentialsRegionAndUser.credentialsAndRegion.region.getName
    val cfTemplateName = samCFTemplateName.replace(".", "-").replace(" ", "")
    val s3BucketName = deploymentBucketName.replace(".", "-").replace(" ", "")
    val streams: List[KinesisStream] = (samResources.streams ++ samResources.s3Firehoses.map(_.stream(projectName, samStage))).toList
    val buckets: List[S3Bucket] = (samResources.buckets ++ samResources.s3Firehoses.map(_.bucket(projectName, samStage))).toList
    val roles: List[IamRole] = (samResources.iamRoles ++ samResources.s3Firehoses.map(_.role)).toList
    ProjectConfiguration(
      projectName,
      projectVersion,
      projectDescription,
      SamS3BucketName(s3BucketName),
      SamCFTemplateName(cfTemplateName),
      SamStage(samStage),
      SamResourcePrefixName(samResourcePrefixName),
      credentialsRegionAndUser,
      amazonUser,
      samResources.authpool,
      samResources.importAuthPool,
      samResources.lambdas.toList,
      samResources.tables.toList,
      samResources.topics.toList,
      streams,
      buckets,
      samResources.s3Firehoses.toList,
      roles,
      samResources.sqlApplications,
    )
  }
}
case class ProjectConfiguration(
                                 projectName: String,
                                 projectVersion: String,
                                 projectDescription: String,
                                 samS3BucketName: SamS3BucketName,
                                 samCFTemplateName: SamCFTemplateName,
                                 samStage: SamStage,
                                 samResourcePrefixName: SamResourcePrefixName,
                                 credentialsRegionAndUser: CredentialsRegionAndUser,
                                 amazonUser: AmazonUser,
                                 authpool: Option[Authpool] = Option.empty,
                                 importAuthPool: Option[ImportAuthPool] = Option.empty,
                                 lambdas: List[LambdaHandler] = List.empty,
                                 tables: List[TableWithIndex] = List.empty,
                                 topics: List[Topic] = List.empty,
                                 streams: List[KinesisStream] = List.empty,
                                 buckets: List[S3Bucket] = List.empty,
                                 s3Firehoses: List[S3Firehose] = List.empty,
                                 iamRoles: List[IamRole] = List.empty,
                                 sqlApplications: List[SqlApplication] = List.empty,
) extends AnyOps {
  def httpHandlers: List[HttpHandler] = lambdas.collect({case h: HttpHandler => h})
  def existHttpHandlers: Boolean = httpHandlers.nonEmpty
  def scheduledEventHandlers: List[ScheduledEventHandler] = lambdas.collect({case h: ScheduledEventHandler => h})
  def snsEventHandlers: List[SNSEventHandler] = lambdas.collect({case h: SNSEventHandler => h})
  def dynamoHandlers: List[DynamoHandler] = lambdas.collect({case h: DynamoHandler => h})
  def kinesisEventHandlers: List[KinesisEventHandler] = lambdas.collect({case h: KinesisEventHandler => h})
  def userArn: Arn = Arn.fromArnString(credentialsRegionAndUser.user.getArn.wrap[Arn])
  def accountId: String = userArn.accountId.value
  def getRegion: Regions = credentialsRegionAndUser.credentialsAndRegion.region
}