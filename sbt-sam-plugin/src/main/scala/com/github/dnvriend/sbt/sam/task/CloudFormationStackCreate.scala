package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

/**
 * Creates the cloud formation stack if it does not exists
 */
object CloudFormationStackCreate {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: DescribeStackResponse,
    client: AmazonCloudFormation,
    log: Logger): Unit = {
    if (describeStackResponse.failure.isDefined) {
      log.info("Creating cloud formation stack")
      CloudFormationOperations.createStack(
        CreateStackSettings(
          CloudFormationTemplates.deploymentBucketTemplate(config),
          StackName(config.samCFTemplateName.value)),
        client
      )
      CloudFormationOperations.createStackEventGenerator(StackName(config.samCFTemplateName.value), client) {
        case CloudFormationEvent(stackStatus, Some(Event(_, _, _, resourceType, status, _, _, _, _, _, _))) =>
          log.info(s"$stackStatus - $resourceType - $status")
        case CloudFormationEvent(stackStatus, None) =>
          log.info(s"$stackStatus - no event")
      }
    } else {
      log.info("Skipping creating cloud formation stack, it already exists")
    }
  }
}
