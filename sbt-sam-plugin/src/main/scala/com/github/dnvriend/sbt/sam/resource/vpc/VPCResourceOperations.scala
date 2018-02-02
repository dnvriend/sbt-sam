package com.github.dnvriend.sbt.sam.resource.vpc

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.cf.resource.lambda.VPCConfig
import com.typesafe.config.Config

import pureconfig._

import scala.collection.JavaConverters._
import scalaz.Scalaz._
import scalaz._

object VPCResourceOperations extends VPCResourceOperations

trait VPCResourceOperations extends FunctionalOps {

  private def extractVPCs(vpc: Config): Disjunction[String, List[VPCConfig]] = {
    vpc.root().keySet().asScala.toList.map(name => (name, vpc.getConfig(name))).map {
      case (cName, conf) => loadConfig[VPCConfig](conf).map(_.copy(id = cName)).disjunction
    }.sequenceU.leftMap(_.toString)
  }

  private def getVPCConfiguration(conf: Config): Disjunction[String, List[VPCConfig]] = for {
    vpcConf <- conf.getConfig("vpcs").safe.leftMap(_.toString)
    vpcs <- extractVPCs(vpcConf)
  } yield vpcs

  def retrieveVPCs(conf: Config): Set[VPCConfig] = {
    getVPCConfiguration(conf).getOrElse(Nil).toSet
  }
}
