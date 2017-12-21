package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.Stack
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

/**
 * Creates the cloud formation stack if it does not exists
 */
object CloudFormationStackCreate {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: Option[Stack],
    client: AmazonCloudFormation,
    log: Logger): Unit = {
    if (describeStackResponse.isEmpty) {
      log.info("Creating cloud formation stack")
      CloudFormationOperations.createStack(
        CreateStackSettings(
          CloudFormationTemplates.deploymentBucketTemplate(config),
          StackName(config.samCFTemplateName.value)),
        client
      )

      CloudFormationOperations.waitForCloudFormation(StackName(config.samCFTemplateName.value), client, log)
    } else {
      log.info("Skipping creating cloud formation stack, it already exists")
    }
  }
}
