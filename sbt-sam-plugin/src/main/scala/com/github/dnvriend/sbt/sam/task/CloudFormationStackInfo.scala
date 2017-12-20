package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{ Stack, StackResource }
import com.github.dnvriend.sbt.aws.task.SamStack
import sbt.util.Logger

import scalaz.Show

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
    stack: Option[Stack],
    client: AmazonCloudFormation,
    log: Logger
  ): Unit = {

    log.info("Stack details:")
    val samStack = stack.map(SamStack.fromStack)
    log.info(samStack.map(SamStack.show.shows).getOrElse("No stack details"))
    log.info("Endpoints:")
    samStack.flatMap(_.serviceEndpoint).foreach { endpoint =>
      config.lambdas.foreach {
        case HttpHandler(_, HttpConf(path, method, auth)) =>
          log.info(s"${method.toUpperCase} - ${endpoint.value}$path")
        case _ =>
      }
    }
  }
}
