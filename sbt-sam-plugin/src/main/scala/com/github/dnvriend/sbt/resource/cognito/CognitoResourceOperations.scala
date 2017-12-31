package com.github.dnvriend.sbt.resource.cognito

import com.github.dnvriend.ops.FunctionalOps
import com.github.dnvriend.sbt.resource.cognito.model.Authpool
import com.typesafe.config.Config
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig

import scalaz.Disjunction
import scalaz.Scalaz._

object CognitoResourceOperations extends CognitoResourceOperations
trait CognitoResourceOperations extends FunctionalOps {

  def retrieveAuthPool(config: Config): Option[Authpool] = {
    val cognitoConfigAttempt: Disjunction[Throwable, Config] = config.getConfig("cognito.AuthPool").safe

    def extractCognito(cognito: Config): Disjunction[ConfigReaderFailures, Authpool] =
      loadConfig[Authpool](cognito).disjunction

    (for {
      cognitoConfig <- cognitoConfigAttempt
      cognito <- extractCognito(cognitoConfig)
    } yield cognito).toOption
  }

}
