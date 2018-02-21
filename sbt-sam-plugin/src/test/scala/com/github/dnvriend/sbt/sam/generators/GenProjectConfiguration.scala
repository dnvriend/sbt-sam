package com.github.dnvriend.sbt.sam.generators

import java.io.File

import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.model.User
import com.github.dnvriend.sbt.aws.domain.IAMDomain.{AwsCredentials, CredentialsProfileAndRegion, CredentialsRegionAndUser, ProfileLocation}
import com.github.dnvriend.sbt.aws.task._
import com.github.dnvriend.sbt.sam.mock.MockAWSCredentials
import com.github.dnvriend.sbt.sam.task._
import org.scalacheck.{Arbitrary, Gen}
import scalaz._
import scalaz.Scalaz._

trait GenProjectConfiguration extends GenGeneric {

  val genSamS3BucketName = for {
    value <- Gen.const("sam-s3-deployment-bucket-name")
  } yield SamS3BucketName(value)

  val genSamCFTemplateName = for {
    value <- genAlphaNonEmpty
  } yield SamCFTemplateName(value)

  val genSamStage = for {
    value <- Gen.const("test")
  } yield SamStage(value)

  val genUser = for {
    path <- genAlphaNonEmpty
    userName <- genAlphaNonEmpty
    userId <- genAlphaNonEmpty
    arn <- Gen.const("arn:aws:iam::0123456789:user/dnvriend-git")
    createDate <- Gen.calendar.map(_.getTime)
  } yield new User(path, userName, userId, arn, createDate)

  val genRegions = for {
    region <- Gen.oneOf(Regions.EU_WEST_1, Regions.EU_WEST_2, Regions.EU_WEST_3, Regions.EU_CENTRAL_1)
  } yield region

  val genAWSCredentials = for {
    accessKeyId <- genAlphaNonEmpty
    secretKey <- genAlphaNonEmpty
  } yield new MockAWSCredentials(accessKeyId, secretKey)

  val genCredentials = for {
    creds <- genAWSCredentials
  } yield AwsCredentials(creds)

  val genFile = for {
    path <- Gen.const("/tmp/foo.txt")
  } yield new File(path)

  val genProfileLocation = for {
    value <- genFile
  } yield ProfileLocation(value)

  val genCredsProfileAndRegion = for {
    credentials <- genCredentials
    profileLocation <- genProfileLocation
    regions <- genRegions
  } yield CredentialsProfileAndRegion(credentials, profileLocation, regions)

  val genCredentialsRegionAndUser = for {
    credsProfileAndRegion <- genCredsProfileAndRegion
    user <- genUser
  } yield CredentialsRegionAndUser(credsProfileAndRegion, user)

  val genPartition = for {
    value <- Gen.const("aws")
  } yield Partition(value)

  val genService = for {
    value <- Gen.const("iam")
  } yield Service(value)

  val genRegion = for {
    region <- genRegions
  } yield Region(region.getName)

  val genAccountId = for {
    value <- Gen.const("0123456789")
  } yield AccountId(value)

  val genResourceType = for {
    value <- Gen.const("user")
  } yield ResourceType(value)

  val genResource = for {
    value <- Gen.const("dnvriend-git")
  } yield Resource(value)

  val genArn = for {
    partition <- genPartition
    service <- genService
    region <- genRegion
    accountId <- genAccountId
    resourceType <- genResourceType
    resource <- genResource
    arnString <- Gen.const("arn:aws:iam::0123456789:user/dnvriend-git")
  } yield Arn(partition, service, region, accountId, resourceType, resource, arnString)

  val genDRegions = for {
    region <- genRegions
  } yield region.right[Throwable]

  val genAmazonUser = for {
    user <- genUser
    arn <- genArn
    dRegions <- genDRegions
  } yield AmazonUser(user, arn, dRegions)

  val genProjectConfiguration = for {
    projectName <- Gen.const("intake-orders")
    organizationName <- Gen.const("com.github.dnvriend")
    projectVersion <- Gen.const("1.0.0-SNAPSHOT")
    projectDescription <- Gen.const("projectDescription")
    bucketName <- genSamS3BucketName
    templateName <- genSamCFTemplateName
    stage <- genSamStage
    credentialsAndUser <- genCredentialsRegionAndUser
    amazonUser <- genAmazonUser
  } yield ProjectConfiguration(
    projectName,
    organizationName,
    projectVersion,
    projectDescription,
    bucketName,
    templateName,
    stage,
    credentialsAndUser,
    amazonUser,
  )

  implicit val arbProjectConfiguration: Arbitrary[ProjectConfiguration] = Arbitrary.apply(genProjectConfiguration)

  val iterCredentialsAndUser: Iterator[CredentialsRegionAndUser] = iterFor(genCredentialsRegionAndUser)

  val iterAmazonUser: Iterator[AmazonUser] = iterFor(genAmazonUser)

  val iterProjectConfig: Iterator[ProjectConfiguration] = iterFor(genProjectConfiguration)
}

