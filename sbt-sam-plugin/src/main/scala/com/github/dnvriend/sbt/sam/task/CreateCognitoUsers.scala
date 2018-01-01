package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult
import com.github.dnvriend.sbt.aws.task.AwsCognitoIdpOperations
import com.github.dnvriend.sbt.util.UserInput
import sbt.util.Logger

import scalaz._
import scalaz.Scalaz._

object CreateCognitoUsers {
  def run(
    config: ProjectConfiguration,
    client: AWSCognitoIdentityProvider,
    log: Logger
  ): Unit = {
    val projectName = config.projectName
    val stage = config.samStage.value
    val result: Disjunction[String, List[AdminRespondToAuthChallengeResult]] = config.authpool.toList.flatMap { authpool =>
      val userpoolName = CloudFormationTemplates.createResourceName(projectName, stage, authpool.name)
      AwsCognitoIdpOperations.findUserPoolWithClients(userpoolName, client).toList.flatMap { userPoolWithClients =>
        authpool.users.map { user =>
          AwsCognitoIdpOperations.adminCreateAndAuthUser(
            user.username,
            user.password,
            userPoolWithClients.userPool.getId,
            userPoolWithClients.userPoolClients.head.getClientId,
            client
          )
        }
      }
    }.sequenceU

    if (result.isLeft) {
      log.info("result: " + result.swap.getOrElse("Unknown error"))
    }
  }

  def getIdToken(
    config: ProjectConfiguration,
    client: AWSCognitoIdentityProvider,
    log: Logger
  ): Unit = {

    val projectName = config.projectName
    val stage = config.samStage.value

    config.authpool.fold(log.info("No auth pool defined")) { authpool =>
      val userpoolName = CloudFormationTemplates.createResourceName(projectName, stage, authpool.name)
      AwsCognitoIdpOperations.findUserPoolWithClients(userpoolName, client).toList.foreach { userPoolWithClients =>
        val username = UserInput.readInput("type username: ")
        val password = UserInput.readInput("type password: ")
        val token = AwsCognitoIdpOperations.getIdToken(
          username,
          password,
          userPoolWithClients.userPool.getId,
          userPoolWithClients.userPoolClients.head.getClientId,
          client
        )
        log.info(token)
      }
    }
  }
}