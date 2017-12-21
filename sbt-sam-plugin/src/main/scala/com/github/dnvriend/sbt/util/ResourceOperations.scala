package com.github.dnvriend.sbt.util

import java.io

import com.github.dnvriend.sbt.sam.task.Models.{ DynamoDb, Policies }
import com.typesafe.config.{ Config, ConfigFactory }
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig
import sbt._

import scala.collection.JavaConverters._
import scalaz._
import scalaz.Scalaz._
import com.github.dnvriend.ops.FunctionalOps

object ResourceOperations extends FunctionalOps {

  def retrieveDynamoDbTables(baseDir: File): Set[DynamoDb.TableWithIndex] = {
    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "sam.conf")
    val dynamoDbConfig = conf.getConfig("dynamodb").safe

    dynamoDbConfig.map { dynamoDb =>

      val result = dynamoDb.root().keySet().asScala.toList.map(name => (name, dynamoDb.getConfig(name))).map {
        case (cName, conf) =>
          val table = loadConfig[DynamoDb.Table](conf).map(_.copy(configName = cName))
          val indicesConf = conf.root().withOnlyKey("global-secondary-indexes").toConfig

          val indices = if (indicesConf.isEmpty) {
            Nil
          } else {
            loadConfig[Map[String, DynamoDb.GlobalSecondaryIndex]](indicesConf.getConfig("global-secondary-indexes")) match {
              case Left(_)  => Nil
              case Right(m) => m.toList.map { case (indexName, index) => index.copy(indexName = indexName) }
            }
          }
          table.map(t => DynamoDb.TableWithIndex(t.name, t.hashKey, t.rangeKey, indices, t.stream, t.rcu, t.wcu, t.configName))
      }

      result.sequenceU.getOrElse(Nil)

    }.getOrElse(Nil).toSet
  }

  def retrievePolicies(baseDir: File): Set[Policies.Policy] = {
    val conf: Config = ConfigFactory.parseFile(baseDir / "conf" / "sam.conf")
    val policiesConfigAttempt: Disjunction[Throwable, Config] = conf.getConfig("policies").safe

    def extractPolicies(policies: Config): Disjunction[ConfigReaderFailures, List[Policies.Policy]] = policies.root().keySet().asScala.toList.map(name => (name, policies.getConfig(name))).map {
      case (cName, conf) =>
        loadConfig[Policies.Policy](conf).map(_.copy(configName = cName)).disjunction
    }.sequenceU

    (for {
      policiesConfig ← policiesConfigAttempt
      policies ← extractPolicies(policiesConfig)
    } yield policies).getOrElse(Nil).toSet
  }
}

