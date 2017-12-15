package com.github.dnvriend.sbt.sam.task

object Models {

  object DynamoDb {

    case class HashKey(name: String, `type`: String = "S")

    case class RangeKey(name: String, `type`: String = "S")

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

}
