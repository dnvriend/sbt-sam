package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.{ AmazonUser, CredentialsAndRegion }

case class SamCFTemplateName(value: String)
case class SamS3BucketName(value: String)
case class SamResourcePrefixName(value: String)
case class SamStage(value: String)
object ProjectConfiguration {
  def fromConfig(
    samS3BucketName: String,
    samCFTemplateName: String,
    samResourcePrefixName: String,
    samStage: String,
    credentialsAndRegion: CredentialsAndRegion,
    amazonUser: AmazonUser): ProjectConfiguration = {
    ProjectConfiguration(
      SamS3BucketName(samS3BucketName),
      SamCFTemplateName(samCFTemplateName),
      SamStage(samStage),
      SamResourcePrefixName(samResourcePrefixName),
      credentialsAndRegion,
      amazonUser
    )
  }
}
case class ProjectConfiguration(
    samS3BucketName: SamS3BucketName,
    samCFTemplateName: SamCFTemplateName,
    samStage: SamStage,
    samResourcePrefixName: SamResourcePrefixName,
    credentialsAndRegion: CredentialsAndRegion,
    amazonUser: AmazonUser
)

//object ProjectConfiguration {
//  // s3 dir structure:
//  // person-repository-dev-sbtsamdeploymentbucket-hex-md5
//  //   |-- sbtsam/person-repository/dev/longmillis-2017-10-23T15:22:16.044Z/compiled-cloudformation-template.json
//  //   |-- sbtsam/person-repository/dev/longmillis-2017-10-23T15:22:16.044Z/person-repository.zip
//
//  // lambda name:
//  // person-repository-dev-post-person
//
//  // cloudformation:
//  // person-repository-dev
//
//}