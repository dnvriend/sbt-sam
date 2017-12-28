package com.github.dnvriend.sbt.resource.kinesis

import com.github.dnvriend.sbt.resource.kinesis.model.KinesisStream
import com.github.dnvriend.ops.FunctionalOps
import com.typesafe.config.Config
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scalaz.Scalaz._

object KinesisResourceOperations extends KinesisResourceOperations
trait KinesisResourceOperations extends FunctionalOps {
  def retrieveStreams(conf: Config): Set[KinesisStream] = {
    conf.getConfig("streams").safe.flatMap { streams =>
      streams.root().keySet().asScala.toList.map(name => (name, streams.getConfig(name))).map {
        case (cName, conf) => loadConfig[KinesisStream](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}
