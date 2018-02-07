package com.github.dnvriend.sbt.sam.resource.authorizer

import com.github.dnvriend.ops.FunctionalOps
import com.typesafe.config.Config

sealed trait AuthorizerType
case object Sigv4AuthorizerType extends AuthorizerType
case object CognitoAuthorizerType extends AuthorizerType

object AuthorizerOperations extends AuthorizerOperations
trait AuthorizerOperations extends FunctionalOps {
  def retrieveAuthorizerType(conf: Config): AuthorizerType = {
    val retrieved = conf.getString("authorizer.type").safe
    retrieved.map(_.toLowerCase).map {
      case "sigv4" => Sigv4AuthorizerType
      case _       => CognitoAuthorizerType
    }
  }.getOrElse(CognitoAuthorizerType)
}
