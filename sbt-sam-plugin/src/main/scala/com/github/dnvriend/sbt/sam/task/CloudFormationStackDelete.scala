package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

object CloudFormationStackDelete {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: DescribeStackResponse,
    client: AmazonCloudFormation,
    log: Logger
  ): Unit = {
    if (describeStackResponse.response.isDefined) {
      log.info("Deleting cloud formation stack")
      CloudFormationOperations.deleteStack(
        DeleteStackSettings(StackName(config.samCFTemplateName.value)),
        client
      )
      CloudFormationOperations.waitForCloudFormation(StackName(config.samCFTemplateName.value), client) {
        case CloudFormationEvent(stackStatus, Some(Event(_, _, _, resourceType, status, _, _, _, _, _, _))) =>
          log.info(s"$stackStatus - $resourceType - $status")
        case _ =>
      }
    } else {
      log.info("Skipping deleting cloud formation stack, it does not exist")
    }
  }
}
