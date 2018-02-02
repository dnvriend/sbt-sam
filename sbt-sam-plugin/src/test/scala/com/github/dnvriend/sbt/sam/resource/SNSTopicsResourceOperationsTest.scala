package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import com.github.dnvriend.test.TestSpec

class SNSTopicsResourceOperationsTest extends TestSpec {
  "sns config" should "read an empty configuration" in {
    ResourceOperations.retrieveTopics("".tsc) shouldBe Set()
  }

  it should "read a sns topic" in {
    ResourceOperations
      .retrieveTopics(
        """
          |topics {
          |   People {
          |    name = "people-stream"
          |    display-name = "people"
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          Topic(
            name = "people-stream",
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
          |    name = "people-stream"
          |    display-name = "people"
          |    export = true
          |  }
          |   People2 {
          |    name = "people-stream2"
          |    display-name = "people2"
          |    export = true
          |  }
          |   People3 {
          |    name = "people-stream3"
          |    display-name = "people3"
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) should contain allOf (
          Topic(
            name = "people-stream",
            configName = "People",
            displayName = "people",
            export = true
          ),
          Topic(
            name = "people-stream2",
            configName = "People2",
            displayName = "people2",
            export = true
          ),
          Topic(
            name = "people-stream3",
            configName = "People3",
            displayName = "people3",
            export = true
          )
        )
  }
}
