package com.github.dnvriend.sbt.util

import com.github.dnvriend.sbt.sam.task.Models.{DynamoDb, Policies}
import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.loadConfig
import sbt._

import scala.collection.JavaConverters._
import scalaz.Scalaz._

object ResourceOperations {
  def retrieveDynamoDbTables(baseDir: File): Set[DynamoDb.TableWithIndex] = {
    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "serverless.conf")
    val dynamoDb = conf.getConfig("dynamoDb")
    val result = dynamoDb.root().keySet().asScala.toList.map(name => (name, dynamoDb.getConfig(name))).map { case (cName, conf) =>
      val table = loadConfig[DynamoDb.Table](conf).map(_.copy(configName = cName))
      val indicesConf = conf.root().withOnlyKey("global-secondary-indexes").toConfig

      val indices = if (indicesConf.isEmpty) {
        Nil
      } else {
        loadConfig[Map[String, DynamoDb.GlobalSecondaryIndex]](indicesConf.getConfig("global-secondary-indexes")) match {
          case Left(_) ⇒ Nil
          case Right(m) ⇒ m.toList.map { case (indexName, index) ⇒ index.copy(indexName = indexName) }
        }
      }

      table.map(t ⇒ DynamoDb.TableWithIndex(t.name, t.hashKey, t.rangeKey, indices, t.stream, t.rcu, t.wcu, t.configName))
    }

    result.sequenceU.getOrElse(Nil).toSet
  }

  def retrievePolicies(baseDir: File): Set[Policies.Policy] = {
    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "serverless.conf")
    val policies = conf.getConfig("policies")

    val result = policies.root().keySet().asScala.toList.map(name ⇒ (name, policies.getConfig(name))).map { case (cName, conf) ⇒
      loadConfig[Policies.Policy](conf).map(_.copy(configName = cName))
    }

    result.sequenceU.getOrElse(Nil).toSet
  }
}