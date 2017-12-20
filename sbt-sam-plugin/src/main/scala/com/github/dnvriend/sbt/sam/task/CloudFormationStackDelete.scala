package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.Stack
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

object CloudFormationStackDelete {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: Option[Stack],
    client: AmazonCloudFormation,
    log: Logger
  ): Unit = {
    if (describeStackResponse.isDefined) {
      log.info("Deleting cloud formation stack")
      CloudFormationOperations.deleteStack(
        DeleteStackSettings(
          StackName(config.samCFTemplateName.value)), client
      )

      CloudFormationOperations.waitForCloudFormation(StackName(config.samCFTemplateName.value), client, log)
    } else {
      log.info("Skipping deleting cloud formation stack, it does not exist")
    }
  }
}
