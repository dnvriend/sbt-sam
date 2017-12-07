package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

/**
 * Update the cloud formation stack, if it exists
 */
object CloudFormationStackUpdate {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: DescribeStackResponse,
    client: AmazonCloudFormation,
    log: Logger): Unit = {
    if (describeStackResponse.response.isDefined) {
      log.info("Updating cloud formation stack")
      CloudFormationOperations.updateStack(
        UpdateStackSettings(
          CloudFormationTemplates.updateTemplate(config),
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
      log.info("Skipping updating cloud formation stack, it does not exist")
    }
  }
}
