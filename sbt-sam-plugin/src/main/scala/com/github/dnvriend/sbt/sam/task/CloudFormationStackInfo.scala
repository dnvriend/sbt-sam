package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.StackResource
import com.github.dnvriend.sbt.aws.task.{ CloudFormationOperations, DescribeStackResourcesSettings, DescribeStackResponse, StackName }
import com.github.dnvriend.sbt.util.Report
import sbt.util.Logger

import scala.collection.JavaConverters._
import scalaz.{ Show, Disjunction }
import scalaz.std.AllInstances._
import com.github.dnvriend.sbt.util.ShowInstances._

object CloudFormationStackResource {
  implicit val show: Show[CloudFormationStackResource] = Show.shows(model => {
    import model._
    s"$logicalResourceId - $resourceType - $resourceStatus - $timestamp"
  })
  def fromStackResource(resource: StackResource): CloudFormationStackResource = {
    CloudFormationStackResource(
      resource.getLogicalResourceId,
      resource.getResourceType,
      resource.getResourceStatus,
      resource.getTimestamp.toString
    )
  }
}
case class CloudFormationStackResource(
    logicalResourceId: String,
    resourceType: String,
    resourceStatus: String,
    timestamp: String)

object CloudFormationStackInfo {
  def run(
    config: ProjectConfiguration,
    describeStackResponse: DescribeStackResponse,
    client: AmazonCloudFormation,
    log: Logger
  ): Unit = {

    log.info("The stack contains the following resources:")
    val result: Disjunction[Throwable, List[CloudFormationStackResource]] = CloudFormationOperations.describeStackResources(
      DescribeStackResourcesSettings(StackName(config.samCFTemplateName.value)),
      client
    ).map(result => result.getStackResources.asScala.map(CloudFormationStackResource.fromStackResource).toList)
    log.info(Report.report(result))
  }
}
