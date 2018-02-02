package com.github.dnvriend.sbt.sam.resource.rds

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.cf.rds.RDSInstance
import com.typesafe.config.Config
import pureconfig._

import scala.collection.JavaConverters._
import scalaz.Scalaz._
import scalaz._

object RDSResourceOperations extends RDSResourceOperations

trait RDSResourceOperations extends FunctionalOps {

  private def extractInstances(rdsInstances: Config): Disjunction[String, List[RDSInstance]] = {
    rdsInstances.root().keySet().asScala.toList.map(name => (name, rdsInstances.getConfig(name))).map {
      case (cName, conf) => loadConfig[RDSInstance](conf).map(_.copy(configName = cName)).disjunction
    }.sequenceU.leftMap(_.toString)
  }

  private def getRDSInstancesFromConfig(conf: Config): Disjunction[String, List[RDSInstance]] = for {
    rdsConf <- conf.getConfig("rds").safe.leftMap(_.toString)
    rdsInstances <- extractInstances(rdsConf)
  } yield rdsInstances

  def retrieveRDSInstances(conf: Config): Set[RDSInstance] = {
    getRDSInstancesFromConfig(conf).getOrElse(Nil).toSet
  }
}
