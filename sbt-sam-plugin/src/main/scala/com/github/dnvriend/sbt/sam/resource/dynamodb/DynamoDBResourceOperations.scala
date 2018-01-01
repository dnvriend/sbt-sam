package com.github.dnvriend.sbt.sam.resource.dynamodb

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.dynamodb.model._
import com.typesafe.config.Config
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scala.language.postfixOps
import scalaz.Disjunction
import scalaz.Scalaz._

object DynamoDBResourceOperations extends DynamoDBResourceOperations
trait DynamoDBResourceOperations extends FunctionalOps {
  def setIndexName(indexName: String, index: GlobalSecondaryIndex): GlobalSecondaryIndex = {
    index.copy(indexName = indexName)
  }
  def getIndices(indicesConf: Config): Disjunction[String, List[GlobalSecondaryIndex]] = for {
    config <- loadConfig[Map[String, GlobalSecondaryIndex]](indicesConf.getConfig("global-secondary-indexes")).safe.leftMap(_.toString)
    indices <- config.disjunction.leftMap(_.toString)
  } yield indices.toList.map(setIndexName _ tupled)

  def getGlobalSecondaryIndexes(conf: Config): List[GlobalSecondaryIndex] = {
    val indicesConf: Config = conf.root().withOnlyKey("global-secondary-indexes").toConfig
    getIndices(indicesConf).getOrElse(Nil)
  }

  def getTableWithIndex(cName: String, conf: Config): Disjunction[ConfigReaderFailures, TableWithIndex] = {
    val table = loadConfig[Table](conf).map(_.copy(configName = cName))
    table.map(t => TableWithIndex(t.name, t.hashKey, t.rangeKey, getGlobalSecondaryIndexes(conf), t.stream, t.rcu, t.wcu, t.configName)).disjunction
  }

  def getTables(dynamoDb: Config): Disjunction[String, List[TableWithIndex]] = {
    dynamoDb.root().keySet().asScala.toList
      .map(name => (name, dynamoDb.getConfig(name)))
      .map(getTableWithIndex _ tupled)
      .sequenceU
      .leftMap(_.toString)
  }

  def getTablesForConfig(conf: Config): Disjunction[String, List[TableWithIndex]] = for {
    dynamoDb <- conf.getConfig("dynamodb").safe.leftMap(_.toString)
    table <- getTables(dynamoDb)
  } yield table

  /**
   * Based on a dynamodb typesafe configuration, returns a set of DynamoDB Tables with Index configuration
   */
  def retrieveDynamoDbTables(conf: Config): Set[TableWithIndex] = {
    getTablesForConfig(conf).getOrElse(Nil).toSet
  }
}
