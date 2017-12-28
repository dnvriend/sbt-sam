package com.github.dnvriend.sbt.resource.policy

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.resource.policy.model._
import com.typesafe.config.Config
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig

import scala.collection.JavaConverters._
import scalaz.Disjunction
import scalaz.Scalaz._

object PolicyResourceOperations extends PolicyResourceOperations
trait PolicyResourceOperations extends FunctionalOps {
  def retrievePolicies(conf: Config): Set[Policy] = {
    val policiesConfigAttempt: Disjunction[Throwable, Config] = conf.getConfig("policies").safe

    def extractPolicies(policies: Config): Disjunction[ConfigReaderFailures, List[Policy]] = policies.root().keySet().asScala.toList.map(name => (name, policies.getConfig(name))).map {
      case (cName, conf) =>
        loadConfig[Policy](conf).map(_.copy(configName = cName)).disjunction
    }.sequenceU

    (for {
      policiesConfig ← policiesConfigAttempt
      policies ← extractPolicies(policiesConfig)
    } yield policies).getOrElse(Nil).toSet
  }
}