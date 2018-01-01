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
          |    compression = "UNCOMPRESSED" // UNCOMPRESSED | GZIP | ZIP | Snappy
          |    shard-count = 1 //
          |    retention-period-hours = 24 // min=24, max=168 (7 days)
          |    buffering-interval-in-seconds = 100 // min=60, max 900; default 300
          |    buffering-size = 1 // default 5, max = 128, min = 1
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          S3Firehose(
            "button-clicked-firehose",
            "ButtonClicked",
            "UNCOMPRESSED",
            1,
            24,
            100,
            1,
            None,
            true
          )
        )
  }
}
