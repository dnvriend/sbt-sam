package com.github.dnvriend.sbt.sam.resource.role

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.role.model._
import com.typesafe.config.Config
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scalaz.Scalaz._

object RoleResourceOperations extends RoleResourceOperations
trait RoleResourceOperations extends FunctionalOps {
  def retrieveRoles(conf: Config): Set[IamRole] = {
    conf.getConfig("roles").safe.flatMap { roles =>
      roles.root().keySet().asScala.toList.map(name => (name, roles.getConfig(name))).map {
        case (cName, conf) => loadConfig[IamRole](conf).map(_.copy(configName = cName)).disjunction
      }.sequenceU
    }.getOrElse(Nil).toSet
  }
}
