package com.github.dnvriend.sbt.util

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.resource.ResourceOperations
import com.github.dnvriend.sbt.resource.dynamodb.model._
import com.github.dnvriend.test.TestSpec

class DynamoDBResourceOperationsTest extends TestSpec with AllOps {
  "dynamodb config" should "read an empty configuration" in {
    ResourceOperations.retrieveDynamoDbTables("".tsc) shouldBe Set()
  }

  it should "read a simple table" in {
    ResourceOperations
      .retrieveDynamoDbTables(
        """
       |dynamodb {
       |   People {
       |    name = people
       |    hash-key = {
       |      name = id
       |      key-type = S
       |    }
       |    rcu = 5
       |    wcu = 4
       |  }
       |}
     """.stripMargin.tsc) shouldBe Set(
          TableWithIndex(
            name = "people",
            hashKey = HashKey("id", "S"),
            rangeKey = None,
            gsis = Nil,
            stream = None,
            rcu = 5,
            wcu = 4,
            configName = "People"
          )
        )
  }

  it should "read a table with stream config" in {
    ResourceOperations
      .retrieveDynamoDbTables(
        """
          |dynamodb {
          | People {
          |    name = people
          |    hash-key = {
          |      name = id
          |      key-type = S
          |    }
          |    stream = KEYS_ONLY
          |    rcu = 5
          |    wcu = 4
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          TableWithIndex(
            name = "people",
            hashKey = HashKey("id", "S"),
            rangeKey = None,
            gsis = Nil,
            stream = Option("KEYS_ONLY"),
            rcu = 5,
            wcu = 4,
            configName = "People"
          )
        )
  }

  it should "read a table with hash and range key" in {
    ResourceOperations
      .retrieveDynamoDbTables(
        """
          |dynamodb {
          | People {
          |      name = people
          |      hash-key = {
          |        name = id
          |        key-type = S
          |      }
          |      range-key = {
          |        name = name
          |        key-type = N
          |      }
          |      rcu = 5
          |      wcu = 4
          |    }
          |}
        """.stripMargin.tsc) shouldBe Set(
          TableWithIndex(
            name = "people",
            hashKey = HashKey("id", "S"),
            rangeKey = Option(RangeKey("name", "N")),
            gsis = Nil,
            stream = None,
            rcu = 5,
            wcu = 4,
            configName = "People"
          )
        )
  }

  it should "read a table with global secondary index with hash key" in {
    ResourceOperations
      .retrieveDynamoDbTables(
        """
          |dynamodb {
          |  People {
          |    name = people
          |    hash-key = {
          |      name = id
          |      key-type = S
          |    }
          |    range-key = {
          |      name = name
          |      key-type = N
          |    }
          |
          |    global-secondary-indexes {
          |      people_id {
          |        hash-key = {
          |          name = id
          |          key-type = S
          |        }
          |
          |        projection-type = ALL
          |        rcu = 3
          |        wcu = 2
          |      }
          |    }
          |    rcu = 5
          |    wcu = 4
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          TableWithIndex(
            name = "people",
            hashKey = HashKey("id", "S"),
            rangeKey = Option(RangeKey("name", "N")),
            gsis = List(
              GlobalSecondaryIndex(
                indexName = "people_id",
                hashKey = HashKey("id", "S"),
                rangeKey = None,
                projectionType = "ALL",
                rcu = 3,
                wcu = 2
              )
            ),
            stream = None,
            rcu = 5,
            wcu = 4,
            configName = "People"
          )
        )
  }

  it should "read a table with global secondary index with hash and range key" in {
    ResourceOperations
      .retrieveDynamoDbTables(
        """
          |dynamodb {
          |  People {
          |    name = people
          |    hash-key = {
          |      name = id
          |      key-type = S
          |    }
          |    range-key = {
          |      name = name
          |      key-type = N
          |    }
          |
          |    global-secondary-indexes {
          |      people_id {
          |        hash-key = {
          |          name = id
          |          key-type = S
          |        }
          |        range-key = {
          |          name = name
          |          key-type = N
          |        }
          |        projection-type = ALL
          |        rcu = 3
          |        wcu = 2
          |      }
          |    }
          |    rcu = 5
          |    wcu = 4
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          TableWithIndex(
            name = "people",
            hashKey = HashKey("id", "S"),
            rangeKey = Option(RangeKey("name", "N")),
            gsis = List(
              GlobalSecondaryIndex(
                indexName = "people_id",
                hashKey = HashKey("id", "S"),
                rangeKey = Option(RangeKey("name", "N")),
                projectionType = "ALL",
                rcu = 3,
                wcu = 2
              )
            ),
            stream = None,
            rcu = 5,
            wcu = 4,
            configName = "People"
          )
        )
  }

  it should "read a table with multiple global secondary indices with hash and range key" in {
    ResourceOperations
      .retrieveDynamoDbTables(
        """
          |dynamodb {
          |  People {
          |    name = people
          |    hash-key = {
          |      name = id
          |      key-type = S
          |    }
          |    range-key = {
          |      name = name
          |      key-type = N
          |    }
          |
          |    global-secondary-indexes {
          |      people_id {
          |        hash-key = {
          |          name = id
          |          key-type = S
          |        }
          |        range-key = {
          |          name = name
          |          key-type = N
          |        }
          |        projection-type = ALL
          |        rcu = 3
          |        wcu = 2
          |      }
          |      people_id_2 {
          |        hash-key = {
          |          name = id
          |          key-type = S
          |        }
          |        range-key = {
          |          name = name
          |          key-type = N
          |        }
          |        projection-type = ALL
          |        rcu = 3
          |        wcu = 2
          |      }
          |    }
          |    rcu = 5
          |    wcu = 4
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          TableWithIndex(
            name = "people",
            hashKey = HashKey("id", "S"),
            rangeKey = Option(RangeKey("name", "N")),
            gsis = List(
              GlobalSecondaryIndex(
                indexName = "people_id",
                hashKey = HashKey("id", "S"),
                rangeKey = Option(RangeKey("name", "N")),
                projectionType = "ALL",
                rcu = 3,
                wcu = 2
              ),
              GlobalSecondaryIndex(
                indexName = "people_id_2",
                hashKey = HashKey("id", "S"),
                rangeKey = Option(RangeKey("name", "N")),
                projectionType = "ALL",
                rcu = 3,
                wcu = 2
              )
            ),
            stream = None,
            rcu = 5,
            wcu = 4,
            configName = "People"
          )
        )
  }
}
