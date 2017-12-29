package com.github.dnvriend.sbt.sam.generators

import java.io.File

import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.model.User
import com.github.dnvriend.sbt.aws.domain.IAMDomain.{AwsCredentials, CredentialsProfileAndRegion, CredentialsRegionAndUser, ProfileLocation}
import com.github.dnvriend.sbt.aws.task._
import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import com.github.dnvriend.sbt.sam.cf.resource.dynamodb._
import com.github.dnvriend.sbt.sam.mock.MockAWSCredentials
import com.github.dnvriend.sbt.sam.resource.dynamodb.model.{GlobalSecondaryIndex, HashKey, RangeKey, TableWithIndex}
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import com.github.dnvriend.sbt.sam.task._
import org.scalacheck.{Arbitrary, _}

import scalaz.Scalaz._

object Generators extends Generators
trait Generators extends GenCFDynamoDBTable
  with GenKinesisStream
  with GenTopic
  with GenGeneric
  with GenLambdaHandler
  with GenTableWithIndex {

  val genSamS3BucketName = for {
    value <- Gen.const("sam-s3-deployment-bucket-name")
  } yield SamS3BucketName(value)

  val genSamCFTemplateName = for {
    value <- genAlphaNonEmpty
  } yield SamCFTemplateName(value)

  val genSamStage = for {
    value <- Gen.const("test")
  } yield SamStage(value)

  val genSamResourcePrefixName = for {
    value <- genAlphaNonEmpty
  } yield SamResourcePrefixName(value)

  val genUser = for {
    path <- genAlphaNonEmpty
    userName <- genAlphaNonEmpty
    userId <- genAlphaNonEmpty
    arn <- genAlphaNonEmpty
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
    value <- genAlphaNonEmpty
  } yield Partition(value)

  val genService = for {
    value <- genAlphaNonEmpty
  } yield Service(value)

  val genRegion = for {
    region <- genRegions
  } yield Region(region.getName)

  val genAccountId = for {
    value <- genAlphaNonEmpty
  } yield AccountId(value)

  val genResourceType = for {
    value <- genAlphaNonEmpty
  } yield ResourceType(value)

  val genResource = for {
    value <- genAlphaNonEmpty
  } yield Resource(value)

  val genArn = for {
    partition <- genPartition
    service <- genService
    region <- genRegion
    accountId <- genAccountId
    resourceType <- genResourceType
    resource <- genResource
    arnString <- genAlphaNonEmpty
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
    projectName <- Gen.const("projectName")
    projectVersion <- Gen.const("projectVersion")
    bucketName <- genSamS3BucketName
    templateName <- genSamCFTemplateName
    stage <- genSamStage
    resourcePrefixName <- genSamResourcePrefixName
    credentialsAndUser <- genCredentialsRegionAndUser
    amazonUser <- genAmazonUser
  } yield ProjectConfiguration(
    projectName,
    projectVersion,
    bucketName,
    templateName,
    stage,
    resourcePrefixName,
    credentialsAndUser,
    amazonUser,
    List.empty,
    List.empty,
    List.empty,
    List.empty,
    List.empty,
  )

  implicit val arbProjectConfiguration: Arbitrary[ProjectConfiguration] = Arbitrary.apply(genProjectConfiguration)

  val iterProjectConfig: Iterator[ProjectConfiguration] = Stream.continually(genProjectConfiguration.sample).collect { case Some(x) => x }.iterator
}

trait GenGeneric {
  val genResourceConfName = for {
    logicalName <- Gen.const("ResourceConfigurationName").flatMap(name => Gen.uuid.map(id => s"$name-$id"))
  } yield logicalName

  val genAlphaNonEmpty = for {
    value <- Gen.alphaStr.suchThat(_.nonEmpty)
  } yield value

  def iterFor[A](gen: Gen[A]): Iterator[A] = Stream.continually(gen.sample).collect { case Some(x) => x }.iterator
}

trait GenTopic extends GenGeneric {
  val genTopic = for {
    name <- Gen.const("snsTopicName")
    configName <- genResourceConfName
    displayName <- Gen.const("displayName")
    topicName <- Gen.const("topicName")
  } yield Topic(name, configName, displayName)

  implicit val arbTopic: Arbitrary[Topic] = Arbitrary.apply(genTopic)

  val iterTopic: Iterator[Topic] = iterFor(genTopic)
}

trait GenKinesisStream extends GenGeneric {
  val genKinesisStream = for {
    name <- Gen.const("kinesisStreamName")
    configName <- genResourceConfName
    retensionPeriodHours <- Gen.posNum[Int]
    shardCount <- Gen.posNum[Int]
  } yield KinesisStream(name, configName, retensionPeriodHours, shardCount)

  implicit val arbKinesisStream: Arbitrary[KinesisStream] = Arbitrary.apply(genKinesisStream)

  val iterKinesisStream: Iterator[KinesisStream] = iterFor(genKinesisStream)
}

object GenCFDynamoDBTable extends GenCFDynamoDBTable
trait GenCFDynamoDBTable extends GenGeneric {
  val genCFDynamoDbTableHashKey = for {
    attrName <- Gen.const("attribute-name-hash-key")
  } yield CFDynamoDbTableHashKey(attrName)

  val genCFDynamoDbTableRangeKey = for {
    attrName <- Gen.const("attribute-name-range-key")
  } yield CFDynamoDbTableRangeKey(attrName)

  val genCFCynamoDBTableKeySchema = for {
    hashKey <- genCFDynamoDbTableHashKey
    rangeKey <- genCFDynamoDbTableRangeKey
  } yield CFDynamoDBTableKeySchema(hashKey, Option(rangeKey))

  val genCFDynamoDBTableAttributeDefinition = for {
    attrName <- Gen.const("attribute-name")
    attrType <- Gen.oneOf("S", "N", "B")
  } yield CFDynamoDBTableAttributeDefinition(attrName, attrType)

  val genCFDynamoDBTableAttributeDefinitions = for {
    attributes <- Gen.containerOfN[List, CFDynamoDBTableAttributeDefinition](2, genCFDynamoDBTableAttributeDefinition)
  } yield CFDynamoDBTableAttributeDefinitions(attributes)

  val genCFDynamoDBTableProvisionedThroughput = for {
    rcu <- Gen.posNum[Int]
    wcu <- Gen.posNum[Int]
  } yield CFDynamoDBTableProvisionedThroughput(rcu, wcu)

  val genCFDynamoDBTableStreamSpecification = for {
    viewType <- Gen.oneOf("KEYS_ONLY", "NEW_IMAGE", "OLD_IMAGE", "NEW_AND_OLD_IMAGES")
  } yield CFDynamoDBTableStreamSpecification(viewType)

  val genCFDynamoDBTableGlobalSecondaryIndex = for {
    indexName <- Gen.const("index-name")
    keySchema <- genCFCynamoDBTableKeySchema
    projectionType <- Gen.oneOf("KEYS_ONLY", "NEW_IMAGE", "OLD_IMAGE", "NEW_AND_OLD_IMAGES")
    throughput <- genCFDynamoDBTableProvisionedThroughput
  } yield CFDynamoDBTableGlobalSecondaryIndex(
    indexName,
    keySchema,
    projectionType,
    throughput
  )

  val genCFDynamoDBTableGlobalSecondaryIndexes = for {
    gsis <- Gen.containerOfN[List, CFDynamoDBTableGlobalSecondaryIndex](2, genCFDynamoDBTableGlobalSecondaryIndex)
  } yield CFDynamoDBTableGlobalSecondaryIndexes(gsis)

  val genCFDynamoDBTableTags = for {
    projectName <- Gen.alphaStr
    projectVersion <- Gen.alphaStr
    stage <- Gen.alphaStr
  } yield CFDynamoDBTableTags(ResourceTag.projectTags(projectName, projectVersion, stage))

  val genCFDynamoDBTableProperties = for {
    tableName <- Gen.const("table_name")
    keySchema <- genCFCynamoDBTableKeySchema
    attrDefinitions <- genCFDynamoDBTableAttributeDefinitions
    throughput <- genCFDynamoDBTableProvisionedThroughput
    streamSpec <- genCFDynamoDBTableStreamSpecification
    gsis <- genCFDynamoDBTableGlobalSecondaryIndexes
    tags <- genCFDynamoDBTableTags
  } yield CFDynamoDBTableProperties(
    CFTDynamoDBTableName(tableName),
    keySchema,
    attrDefinitions,
    throughput,
    Option(streamSpec),
    Option(gsis),
    tags
  )

  val genCFDynamoDBTable = for {
    logicalName <- Gen.const("DynamoDBTable")
    props <- genCFDynamoDBTableProperties
  } yield CFDynamoDBTable(logicalName, props)

  implicit val arbCFDynamoDBTable: Arbitrary[CFDynamoDBTable] = Arbitrary.apply(genCFDynamoDBTable)

  val iterCFTable: Iterator[CFDynamoDBTable] = iterFor(genCFDynamoDBTable)
}

trait GenTableWithIndex extends GenGeneric {
  val genHashKey = for {
    name <- Gen.const("hash-key-name")
    keyType <- Gen.oneOf("S", "N", "B")
  } yield HashKey(name, keyType)

  val genRangeKey = for {
    name <- Gen.const("range-key-name")
    keyType <- Gen.oneOf("S", "N", "B")
  } yield RangeKey(name, keyType)

  val genGlobalSecondaryIndex = for {
    name <- Gen.const("gsi-name")
    hashKey <- genHashKey
    rangeKey <- genRangeKey
    projectionType <- Gen.oneOf("KEYS_ONLY", "NEW_IMAGE", "OLD_IMAGE", "NEW_AND_OLD_IMAGES")
  } yield GlobalSecondaryIndex(
    name,
    hashKey,
    Option(rangeKey),
    projectionType,
    3,
    2
  )

  val genTableWithIndex = for {
    confName <- genResourceConfName
    name <- Gen.const("table_name")
    hashKey <- genHashKey
    rangeKey <- genRangeKey
    gsi <- Gen.containerOfN[List, GlobalSecondaryIndex](2, genGlobalSecondaryIndex)
  } yield TableWithIndex(
    name,
    hashKey,
    Option(rangeKey),
    gsi,
    Option("stream"),
    3,
    2,
    confName
  )

  implicit val arbDynamoDBTable: Arbitrary[TableWithIndex] = Arbitrary.apply(genTableWithIndex)

  val iterTableWithIndex: Iterator[TableWithIndex] = iterFor(genTableWithIndex)
}

trait GenLambdaHandler extends GenGeneric {
  val genLambdaConf = for {
    fqcn <- Gen.const("fqcn")
    simpleClassName <- genResourceConfName
    memorySize <- Gen.const(1024)
    timeout <- Gen.const(300)
    description <- genAlphaNonEmpty
  } yield LambdaConfig(
    fqcn,
    simpleClassName,
    memorySize,
    timeout,
    description
  )
  val genHttpHandler = for {
    lambdaConf <- genLambdaConf
    path <- Gen.uuid.map(id => s"/id/$id")
    method <- Gen.oneOf("get", "post", "put", "patch", "delete", "head")
  } yield HttpHandler(
    lambdaConf,
    HttpConf(
      path,
      method
    )
  )
  implicit val arbHttpHandler: Arbitrary[HttpHandler] = Arbitrary.apply(genHttpHandler)

  val iterHttpHandler: Iterator[HttpHandler] = iterFor(genHttpHandler)

  val genDynamoHandler = for {
    lambdaConf <- genLambdaConf
  } yield DynamoHandler(
    lambdaConf,
    DynamoConf()
  )

  val iterDynamoHandler: Iterator[DynamoHandler] = iterFor(genDynamoHandler)


  val genScheduledEventHandler = for {
    lambdaConf <- genLambdaConf
    schedule <- Gen.alphaStr
  } yield ScheduledEventHandler(
    lambdaConf,
    ScheduleConf(schedule)
  )

  val iterScheduledEventHandler: Iterator[ScheduledEventHandler] = iterFor(genScheduledEventHandler)

  val genSNSEventHandler = for {
    lambdaConf <- genLambdaConf
    topic <- Gen.alphaStr
  } yield SNSEventHandler(
    lambdaConf,
    SNSConf(topic)
  )

  val iterSNSEventHandler: Iterator[SNSEventHandler] = iterFor(genSNSEventHandler)

  val genKinesisEventHandler = for {
    lambdaConf <- genLambdaConf
    stream <- Gen.alphaStr
  } yield KinesisEventHandler(
    lambdaConf,
    KinesisConf(stream)
  )

  val iterKinesisEventHandler: Iterator[KinesisEventHandler] = iterFor(genKinesisEventHandler)
}
