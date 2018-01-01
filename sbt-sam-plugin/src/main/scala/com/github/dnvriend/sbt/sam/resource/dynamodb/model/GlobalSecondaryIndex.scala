package com.github.dnvriend.sbt.sam.resource.dynamodb.model

case class GlobalSecondaryIndex(indexName: String = "", hashKey: HashKey, rangeKey: Option[RangeKey], projectionType: String = "", rcu: Int = 1, wcu: Int = 1)
