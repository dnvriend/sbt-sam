package com.github.dnvriend.sbt.sam.task

object Models {

  object DynamoDb {

    case class HashKey(name: String, keyType: String = "S")

    case class RangeKey(name: String, keyType: String = "S")

    case class GlobalSecondaryIndex(indexName: String = "", hashKey: HashKey, rangeKey: Option[RangeKey], projectionType: String = "", rcu: Int = 1, wcu: Int = 1)

    case class Table(name: String, hashKey: HashKey, rangeKey: Option[RangeKey] = None, stream: Option[String] = None, rcu: Int = 1, wcu: Int = 1, configName: String = "")

    case class TableWithIndex(name: String, hashKey: HashKey, rangeKey: Option[RangeKey] = None, gsis: List[GlobalSecondaryIndex] = Nil, stream: Option[String] = None, rcu: Int = 1, wcu: Int = 1, configName: String = "")

  }

  object Policies {

    case class Statements(allowedActions: List[String], resource: String)

    case class Role(ref: String)

    case class Properties(name: String, roles: List[Role], statements: List[Statements])

    case class Policy(configName: String = "", dependsOn: String, properties: Properties)

  }

  object SNS {
    case class Topic(
        name: String,
        configName: String = "",
        displayName: String = "",
        // Export the resource to other components. Default value is false
        export: Boolean = false
    )
  }

  object Kinesis {
    case class Stream(
        name: String,
        configName: String = "",
        // The number of hours for the data records that are stored in shards to remain accessible.
        // The default value is 24.
        retensionPeriodHours: Int = 24,
        // The number of shards that the stream uses. For greater provisioned throughput, increase the number of shards.
        shardCount: Int = 1,
        // Export the resource to other components. Default value is false
        export: Boolean = false
    )
  }
}
