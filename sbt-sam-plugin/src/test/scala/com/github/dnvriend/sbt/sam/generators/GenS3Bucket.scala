package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.bucket.model.{ S3Bucket, S3Website }
import org.scalacheck.{ Arbitrary, Gen }

trait GenS3Bucket extends GenGeneric {
  val genS3Website = for {
    indexDoc <- Gen.const("index.html")
    errDoc <- Gen.const("error.html")
  } yield S3Website(indexDoc, errDoc)

  val genS3Bucket = for {
    name <- Gen.const("S3BucketName")
    accessControl <- Gen.const("Private")
    configName <- genResourceConfName
    website <- genS3Website
  } yield S3Bucket(
    name,
    accessControl,
    configName,
    Some(website),
    true,
    true,
    true,
    true
  )

  val iterS3Bucket: Iterator[S3Bucket] = iterFor(genS3Bucket)
}