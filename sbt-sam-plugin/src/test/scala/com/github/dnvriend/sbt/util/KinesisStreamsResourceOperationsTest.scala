package com.github.dnvriend.sbt.util

import com.github.dnvriend.sbt.sam.task.Models.Kinesis
import com.github.dnvriend.test.TestSpec

class KinesisStreamsResourceOperationsTest extends TestSpec {
  "kinesis streams config" should "read a stream" in {
    ResourceOperations
      .retrieveStreams(
        """
          |streams {
          |   People {
          |     name = "people-stream"
          |     retension-period-hours = 48
          |     shard-count = 10
          |     export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          Kinesis.Stream(
            name = "people-stream",
            configName = "People",
            retensionPeriodHours = 48,
            shardCount = 10,
            export = true
          )
        )
  }

  it should "read multiple streams" in {
    ResourceOperations
      .retrieveStreams(
        """
          |streams {
          |   People {
          |     name = "people-stream"
          |     retension-period-hours = 48
          |     export = true
          |  }
          |   People2 {
          |     name = "people-stream2"
          |     retension-period-hours = 24
          |     export = true
          |  }
          |   People3 {
          |     name = "people-stream3"
          |     retension-period-hours = 12
          |     export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          Kinesis.Stream(
            name = "people-stream",
            configName = "People",
            retensionPeriodHours = 48,
            shardCount = 1,
            export = true
          ),
          Kinesis.Stream(
            name = "people-stream2",
            configName = "People2",
            retensionPeriodHours = 24,
            export = true
          ),
          Kinesis.Stream(
            name = "people-stream3",
            configName = "People3",
            retensionPeriodHours = 12,
            export = true
          )
        )
  }
}
