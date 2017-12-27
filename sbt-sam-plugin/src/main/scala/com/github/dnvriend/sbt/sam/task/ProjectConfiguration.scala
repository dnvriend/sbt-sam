package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.domain.IAMDomain.CredentialsRegionAndUser
import com.github.dnvriend.sbt.aws.task.AmazonUser
import com.github.dnvriend.sbt.sam.task.Models.{DynamoDb, Kinesis, Policies, SNS}

import scala.util.matching.Regex

case class SamCFTemplateName(value: String) {
  val templateNameRegex: Regex = """[a-zA-Z][-a-zA-Z0-9]*""".r
  require(templateNameRegex.findFirstIn(value).isDefined, s"CloudFormation template name, with name '$value', must satisfy regular expression pattern: [a-zA-Z][-a-zA-Z0-9]*")
}
case class SamS3BucketName(value: String) {
  val bucketNameRegex: Regex = """^([a-z]|(\d(?!\d{0,2}\.\d{1,3}\.\d{1,3}\.\d{1,3})))([a-z\d]|(\.(?!(\.|-)))|(-(?!\.))){1,61}[a-z\d\.]$""".r
  require(bucketNameRegex.findFirstIn(value).exists(name => name.length > 3 && name.length < 63), s"Bucket name with name '$value', should be between 3 and 63 characters long")
}
case class SamResourcePrefixName(value: String)
case class SamStage(value: String) {
  require(!List("-", ".", " ", "/").exists(char => value.contains(char)), s"sam stage with value '$value', should not contain '.', '-', '/' or spaces")
}
case class SamResources(lambdas: Set[LambdaHandler],
                        tables: Set[DynamoDb.TableWithIndex],
                        policies: Set[Policies.Policy],
                        topics: Set[SNS.Topic],
                        streams: Set[Kinesis.Stream]
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
      samResources.lambdas,
      samResources.tables,
      samResources.policies,
      samResources.topics,
      samResources.streams
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
    lambdas: Set[LambdaHandler],
    tables: Set[DynamoDb.TableWithIndex],
    policies: Set[Policies.Policy],
    topics: Set[SNS.Topic],
    streams: Set[Kinesis.Stream]
)