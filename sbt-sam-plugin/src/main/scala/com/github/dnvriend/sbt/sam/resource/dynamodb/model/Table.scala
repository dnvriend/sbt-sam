package com.github.dnvriend.sbt.sam.resource.dynamodb.model

case class Table(name: String, hashKey: HashKey, rangeKey: Option[RangeKey] = None, stream: Option[String] = None, rcu: Int = 1, wcu: Int = 1, configName: String = "")
