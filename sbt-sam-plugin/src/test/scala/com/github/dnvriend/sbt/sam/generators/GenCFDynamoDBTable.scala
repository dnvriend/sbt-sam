package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import com.github.dnvriend.sbt.sam.cf.resource.dynamodb._
import org.scalacheck.{ Arbitrary, Gen }

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