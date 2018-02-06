package com.github.dnvriend.sbt.sam.resource.function

import com.github.dnvriend.ops.FunctionalOps
import com.typesafe.config.Config

import pureconfig._

import scalaz._
import scalaz.Scalaz._

object FunctionResourceOperations extends FunctionResourceOperations

trait FunctionResourceOperations extends FunctionalOps {

  private def getEnvVarsConfig(conf: Config): \/[String, Config] = {
    conf.getConfig("lambda-env-vars").safe.leftMap(_.toString)
  }

  private def configToMap(conf: Config): \/[String, Map[String, String]] = for {
    envVarsConf <- getEnvVarsConfig(conf)
    envVarsMap <- loadConfig[Map[String, String]](envVarsConf).disjunction.leftMap(_.toString)
  } yield envVarsMap

  def resolveEnvVars(conf: Config): Map[String, String] =
    configToMap(conf).getOrElse(Map.empty)
}

