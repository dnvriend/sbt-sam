package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.dynamodb.model.{ GlobalSecondaryIndex, HashKey, RangeKey, TableWithIndex }
import org.scalacheck.{ Arbitrary, Gen }

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

