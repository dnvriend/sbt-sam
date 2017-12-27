package com.github.dnvriend.sbt.sam.task

import java.util.UUID

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{ Capability, ExecuteChangeSetRequest, Stack }
import com.amazonaws.services.s3.AmazonS3
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

/**
 * Update the cloud formation stack, if it exists
 */
object CloudFormationStackUpdate {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: Option[Stack],
    client: AmazonCloudFormation,
    jarName: String,
    s3Client: AmazonS3,
    log: Logger): Unit = {
    if (describeStackResponse.isDefined) {
      log.info("Updating cloud formation stack")
      val changeSetName = "sam-change-set" + UUID.randomUUID()

      val latestVersion: Option[S3ObjectVersionId] = S3Operations.latestVersion(
        ListVersionsSettings(
          BucketName(config.samS3BucketName.value),
          S3ObjectKey(jarName)
        ), s3Client)

      val settings = CreateChangeSetSettings(
        CloudFormationTemplates.updateTemplate(config, jarName, latestVersion.get.value),
        StackName(config.samCFTemplateName.value),
        ChangeSetName(changeSetName),
        Capability.CAPABILITY_IAM
      )

      val changeSetResult = CloudFormationOperations.createChangeSet(settings, client).valueOr(t => throw t)

      val latestEvent: ChangeSetEvent = CloudFormationOperations.waitForChangeSetAvailable(settings.stackName, settings.changeSetName, client) { event =>
        log.info(s"Change set status: ${event.status} - execution status: ${event.executionStatus} - " + Option(event.statusReason).filter(_ != "null").getOrElse(""))
      }

      if (latestEvent.executionStatus != "UNAVAILABLE") {
        log.info(s"Executing change set: '$changeSetName'")
        val executeResult = client.executeChangeSet(new ExecuteChangeSetRequest()
          .withChangeSetName(settings.changeSetName.value)
          .withStackName(settings.stackName.value)
        )

        CloudFormationOperations.waitForCloudFormation(StackName(config.samCFTemplateName.value), client, log)
      }
    } else {
      log.info("Skipping updating cloud formation stack, it does not exist")
    }
  }
}
