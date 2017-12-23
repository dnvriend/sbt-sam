package com.github.dnvriend.sbt.util

import com.github.dnvriend.sbt.sam.task.Models.{ DynamoDb, Policies, SNS, Kinesis }
import com.typesafe.config.{ Config, ConfigFactory }
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig
import sbt._

import scala.collection.JavaConverters._
import scalaz._
import scalaz.Scalaz._
import com.github.dnvriend.ops.FunctionalOps

object ResourceOperations extends FunctionalOps
  with DynamoDBResourceOperations
  with PolicyResourceOperations
  with SNSResourceOperations
  with KinesisResourceOperations {
  /**
   * Loads the resource configuration from base path
   */
  def readConfig(baseDir: File): Config = {
    ConfigFactory.parseFile(baseDir / "conf" / "sam.conf")
  }
}

trait DynamoDBResourceOperations extends FunctionalOps {
  /**
   * Based on a dynamodb typesafe configuration, returns a set of DynamoDB Tables with Index configuration
   */
  def retrieveDynamoDbTables(conf: Config): Set[DynamoDb.TableWithIndex] = {
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
}

trait PolicyResourceOperations extends FunctionalOps {
  def retrievePolicies(conf: Config): Set[Policies.Policy] = {
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

trait SNSResourceOperations extends FunctionalOps {
  def retrieveTopics(conf: Config): Set[SNS.Topic] = {
    conf.getConfig("topics").safe.flatMap { topics =>
      topics.root().keySet().asScala.toList.map(name => (name, topics.getConfig(name))).map {
        case (cName, conf) => loadConfig[SNS.Topic](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}

trait KinesisResourceOperations extends FunctionalOps {
  def retrieveStreams(conf: Config): Set[Kinesis.Stream] = {
    conf.getConfig("streams").safe.flatMap { streams =>
      streams.root().keySet().asScala.toList.map(name => (name, streams.getConfig(name))).map {
        case (cName, conf) => loadConfig[Kinesis.Stream](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}
