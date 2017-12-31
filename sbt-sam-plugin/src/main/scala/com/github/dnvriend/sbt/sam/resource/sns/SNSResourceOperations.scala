package com.github.dnvriend.sbt.sam.resource.sns

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import com.typesafe.config.Config
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scalaz.Scalaz._

object SNSResourceOperations extends SNSResourceOperations
trait SNSResourceOperations extends FunctionalOps {
  def retrieveTopics(conf: Config): Set[Topic] = {
    conf.getConfig("topics").safe.flatMap { topics =>
      topics.root().keySet().asScala.toList.map(name => (name, topics.getConfig(name))).map {
        case (cName, conf) => loadConfig[Topic](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}