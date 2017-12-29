package com.github.dnvriend.sbt.sam.resource.firehose.s3

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import com.typesafe.config.Config
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scalaz.Scalaz._

object S3FirehoseResourceOperations extends S3FirehoseResourceOperations
trait S3FirehoseResourceOperations extends FunctionalOps {
  def retrieveS3Firehose(conf: Config): Set[S3Firehose] = {
    conf.getConfig("s3firehose").safe.flatMap { firehose =>
      firehose.root().keySet().asScala.toList.map(name => (name, firehose.getConfig(name))).map {
        case (cName, conf) => loadConfig[S3Firehose](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}
