package com.github.dnvriend.sbt.sam.resource.cognito

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.sam.resource.cognito.model.{ Authpool, ImportAuthPool }
import com.typesafe.config.Config
import pureconfig.loadConfig

import scalaz.Disjunction
import scalaz.Scalaz._

object CognitoResourceOperations extends CognitoResourceOperations

trait CognitoResourceOperations extends FunctionalOps {

  def retrieveAuthPool(config: Config): Option[Authpool] = {
    (for {
      cognitoConfig <- cognitoConfigAttempt(config)
      cognito <- loadConfig[Authpool](cognitoConfig).disjunction
    } yield cognito).toOption
  }

  def retrieveImportAuthPool(config: Config, authpool: Option[Authpool]): Option[ImportAuthPool] = {
    val impAuthPool: Option[ImportAuthPool] = (for {
      cognitoConfig <- cognitoConfigAttempt(config)
      cognito <- loadConfig[ImportAuthPool](cognitoConfig).disjunction
    } yield cognito).toOption

    require(
      (authpool.nonEmpty && impAuthPool.isEmpty) || (authpool.isEmpty && impAuthPool.isEmpty) || (authpool.isEmpty && impAuthPool.nonEmpty),
      "A new Authpool should not be defined in the config, when an ImportAuthPool is being used."
    )

    impAuthPool
  }

  private def cognitoConfigAttempt(config: Config): Disjunction[Throwable, Config] = config.getConfig("cognito.AuthPool").safe
}
