package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.Stack
import com.amazonaws.services.s3.AmazonS3
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
    s3Client: AmazonS3,
    log: Logger,
  ): Unit = {
    val deploymentBucketName: String = config.samS3BucketName.value

    if(s3Client.doesBucketExistV2(deploymentBucketName)) {
      throw new RuntimeException(s"Deployment bucket '$deploymentBucketName' already exists, please change the samStage or organization/project name")
    }

    if (describeStackResponse.isEmpty) {
      log.info("Creating cloud formation stack")
      CloudFormationOperations.createStack(
        CreateStackSettings(
          CloudFormationTemplates.deploymentBucketTemplate(config),
          StackName(config.samCFTemplateName.value),
          "sbt:sam:projectName" -> config.projectName,
          "sbt:sam:projectVersion" -> config.projectVersion,
          "sbt:sam:stage" -> config.samStage.value
        ),
        client
      ).valueOr(t => throw t)
      CloudFormationOperations.waitForCloudFormation(StackName(config.samCFTemplateName.value), client, log)
    } else {
      log.info("Skipping creating cloud formation stack, it already exists")
    }
  }
}
