package com.github.dnvriend.sbt.sam.task

import java.util.UUID

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{ Capability, DescribeChangeSetRequest, ExecuteChangeSetRequest }
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

      val changeSetName = "sam-change-set" + UUID.randomUUID()

      val settings = CreateChangeSetSettings(
        CloudFormationTemplates.updateTemplate(config),
        StackName(config.samCFTemplateName.value),
        ChangeSetName(changeSetName),
        Capability.CAPABILITY_IAM
      )

      val changeSetResult = CloudFormationOperations.createChangeSet(settings, client)

      Thread.sleep(5000)

      val csdescribe = client.describeChangeSet(
        new DescribeChangeSetRequest()
          .withStackName(settings.stackName.value)
          .withChangeSetName(settings.changeSetName.value)
      )

      Thread.sleep(5000)

      val executeResult = client.executeChangeSet(new ExecuteChangeSetRequest()
        .withChangeSetName(settings.changeSetName.value)
        .withStackName(settings.stackName.value)

      )

      //      CloudFormationOperations.updateStack(
      //        UpdateStackSettings(
      //          CloudFormationTemplates.updateTemplate(config),
      //          StackName(config.samCFTemplateName.value)),
      //        client
      //      )
      //      CloudFormationOperations.createStackEventGenerator(StackName(config.samCFTemplateName.value), client) {
      //        println
      //      }
      //      CloudFormationOperations.createStackEventGenerator(StackName(config.samCFTemplateName.value), client) {
      //        case CloudFormationEvent(stackStatus, Some(Event(_, _, _, resourceType, status, _, _, _, _, _, _))) =>
      //          log.info(s"$stackStatus - $resourceType - $status")
      //        case CloudFormationEvent(stackStatus, None) =>
      //          log.info(s"$stackStatus - no event")
      //      }
    } else {
      log.info("Skipping updating cloud formation stack, it does not exist")
    }
  }
}
