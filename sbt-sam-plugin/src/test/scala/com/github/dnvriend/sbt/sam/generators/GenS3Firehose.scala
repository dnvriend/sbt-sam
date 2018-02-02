package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import org.scalacheck.{ Arbitrary, Gen }

trait GenS3Firehose extends GenGeneric {
  val genS3Firehose = for {
    name <- Gen.const("s3-firehose-delivery-stream-name")
    configName <- genResourceConfName
    compression <- Gen.oneOf("UNCOMPRESSED", "GZIP", "ZIP", "Snappy")
  } yield S3Firehose(
    name,
    configName,
    compression,
    1,
    24,
    300,
    5,
    export = true
  )

  implicit val arbS3Firehose: Arbitrary[S3Firehose] = Arbitrary.apply(genS3Firehose)

  val iterS3Firehose: Iterator[S3Firehose] = iterFor(genS3Firehose)
}