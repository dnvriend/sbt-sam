package com.github.dnvriend.sbt.util

import com.github.dnvriend.sbt.sam.task.Models.SNS.Topic
import com.github.dnvriend.test.TestSpec

class SNSTopicsResourceOperationsTest extends TestSpec {
  "sns config" should "read an sns topic" in {
    ResourceOperations
      .retrieveTopics(
        """
          |topics {
          |   People {
          |    display-name = "people"
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          Topic(
            configName = "People",
            displayName = "people",
            export = true
          )
        )
  }

  it should "read multiple sns topics" in {
    ResourceOperations
      .retrieveTopics(
        """
          |topics {
          |   People {
          |    display-name = "people"
          |    export = true
          |  }
          |   People2 {
          |    display-name = "people2"
          |    export = true
          |  }
          |   People3 {
          |    display-name = "people3"
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          Topic(
            configName = "People",
            displayName = "people",
            export = true
          ),
          Topic(
            configName = "People2",
            displayName = "people2",
            export = true
          ),
          Topic(
            configName = "People3",
            displayName = "people3",
            export = true
          )
        )
  }
}
