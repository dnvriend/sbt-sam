package com.github.dnvriend.sbt.sam.resource.bucket

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.typesafe.config.Config
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scalaz.Scalaz._

object S3BucketResourceOperations extends S3BucketResourceOperations
trait S3BucketResourceOperations extends FunctionalOps {
  def retrieveBuckets(conf: Config): Set[S3Bucket] = {
    conf.getConfig("buckets").safe.flatMap { buckets =>
      buckets.root().keySet().asScala.toList.map(name => (name, buckets.getConfig(name))).map {
        case (cName, conf) => loadConfig[S3Bucket](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}
