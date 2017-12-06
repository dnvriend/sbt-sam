package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.github.dnvriend.sbt.aws.task.{ CloudFormationOperations, DescribeStackResourcesSettings, DescribeStackResponse, StackName }
import sbt.util.Logger
import scala.collection.JavaConverters._

object CloudFormationStackInfo {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: DescribeStackResponse,
    client: AmazonCloudFormation,
    log: Logger
  ): Unit = {

    log.info("The stack contains the following resources:")
    val msg = CloudFormationOperations.describeStackResources(
      DescribeStackResourcesSettings(StackName(config.samCFTemplateName.value)),
      client
    ).map { result =>
        result.getStackResources.asScala.map { resource =>
          s"${resource.getLogicalResourceId} - ${resource.getResourceType} - ${resource.getResourceStatus} - ${resource.getResourceStatus} - ${resource.getTimestamp}"
        }.mkString("\n")
      }.valueOr(t => s"Error: ${t.getMessage}")
    log.info(msg)
  }

}
