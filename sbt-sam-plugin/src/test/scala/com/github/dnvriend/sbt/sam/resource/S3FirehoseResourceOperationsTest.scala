package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import com.github.dnvriend.test.TestSpec

class S3FirehoseResourceOperationsTest extends TestSpec {
  "s3 firehose config" should "read an empty configuration" in {
    ResourceOperations.retrieveTopics("".tsc) shouldBe Set()
  }

  it should "read a s3 Firehose configuration" in {
    ResourceOperations
      .retrieveS3Firehose(
        """
          |s3firehoses {
          |   ButtonClicked {
          |    name = "button-clicked-firehose" // A name for the delivery stream.
          |    buffering-interval-in-seconds = 100 // min=60, max 900; default 300
          |    buffering-size = 1 // default 5, max = 128, min = 1
          |    bucket-name = "name-of-existing-bucket" // name of the existing bucket -> bucket arn
          |    kinesis-stream-source = "arn-of-kinesis-stream"
          |    compression = "uncompressed" // UNCOMPRESSED | GZIP | ZIP | Snappy
          |    encryption-key = "arn-of-cmk" // arn of CMK
          |    role-arn = "role-arn" // The Amazon Resource Name (ARN) of the AWS credentials.
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          S3Firehose(
            "button-clicked-firehose",
            "name-of-existing-bucket",
            "role-arn",
            "arn-of-kinesis-stream",
            "ButtonClicked",
            Some("uncompressed"),
            Some("arn-of-cmk"),
            100,
            1,
            true
          )
        )
  }
}
