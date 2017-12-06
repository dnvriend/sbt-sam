package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cloudformation._
import com.amazonaws.services.cloudformation.model._
import com.github.dnvriend.ops.Converter

import scalaz.Disjunction

object TemplateBody {
  implicit val toValidateTemplateRequest: Converter[TemplateBody, ValidateTemplateRequest] =
    Converter.instance(template => new ValidateTemplateRequest().withTemplateBody(template.value))
}

final case class TemplateBody(value: String) {
  require(value.nonEmpty, "template body should not be empty")
}

object CreateStackSettings {
  implicit val toCreateStackRequest: Converter[CreateStackSettings, CreateStackRequest] =
    Converter.instance(settings => {
      new CreateStackRequest()
        .withStackName(settings.stackName.value)
        .withTemplateBody(settings.template.value)
    })
}

final case class StackName(value: String) {
  require(value.nonEmpty, "Stack name should not be empty")
}

final case class CreateStackSettings(template: TemplateBody, stackName: StackName)

object CreateStackResponse {
  val zero = CreateStackResponse(None, None)
}

final case class CreateStackResponse(response: Option[CreateStackResult], failure: Option[Throwable])

object UpdateStackSettings {
  implicit val toUpdateStackRequest: Converter[UpdateStackSettings, UpdateStackRequest] =
    Converter.instance(settings => {
      new UpdateStackRequest()
        .withStackName(settings.stackName.value)
        .withTemplateBody(settings.template.value)
    })
}

final case class UpdateStackSettings(template: TemplateBody, stackName: StackName)

object DeleteStackSettings {
  implicit val toDeleteStackRequest: Converter[DeleteStackSettings, DeleteStackRequest] =
    Converter.instance(settings => {
      new DeleteStackRequest()
        .withStackName(settings.stackName.value)
    })

  def fromStackName(stackName: String): DeleteStackSettings = {
    DeleteStackSettings(StackName(stackName))
  }
}

final case class DeleteStackSettings(stackName: StackName)

object DeleteStackResponse {
  val zero = DeleteStackResponse(None, None)
}

final case class DeleteStackResponse(response: Option[DeleteStackResult], failure: Option[Throwable])

object DescribeStackSettings {
  implicit val toDescribeStacksRequest: Converter[DescribeStackSettings, DescribeStacksRequest] =
    Converter.instance(settings => {
      new DescribeStacksRequest()
        .withStackName(settings.stackName.value)
    })

  def fromStackName(stackName: String): DescribeStackSettings = {
    DescribeStackSettings(StackName(stackName))
  }
}

final case class DescribeStackSettings(stackName: StackName)

final case class DescribeStackResponse(response: Option[DescribeStacksResult], failure: Option[Throwable])

object DescribeStackEventsSettings {
  implicit val toDescribeStackEventsSettings: Converter[DescribeStackEventsSettings, DescribeStackEventsRequest] =
    Converter.instance(settings => {
      new DescribeStackEventsRequest()
        .withStackName(settings.stackName.value)
    })

  def fromStackName(stackName: String): DescribeStackEventsSettings = {
    DescribeStackEventsSettings(StackName(stackName))
  }
}

final case class DescribeStackEventsSettings(stackName: StackName)

final case class DescribeStackEventsResponse(response: Option[DescribeStackEventsResult], failure: Option[Throwable])

object CloudFormationOperations extends AwsProgressListenerOps {
  def client(cr: CredentialsAndRegion): AmazonCloudFormation = {
    AmazonCloudFormationClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  /**
   * Validates a specified template. AWS CloudFormation first checks if the template is valid JSON. If it isn't, AWS CloudFormation checks if the template is valid YAML. If both these checks fail, AWS CloudFormation returns a template validation error.
   */
  def validateTemplate(
    templateBody: TemplateBody,
    client: AmazonCloudFormation)(implicit conv: Converter[TemplateBody, ValidateTemplateRequest]): Disjunction[Throwable, ValidateTemplateResult] = {
    Disjunction.fromTryCatchNonFatal(client.validateTemplate(conv(templateBody)))
  }

  /**
   * Creates a stack as specified in the template. After the call completes successfully, the stack creation starts.
   * You can check the status of the stack via the DescribeStacks API. Does not work for templates with transforms.
   */
  def createStack(
    settings: CreateStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[CreateStackSettings, CreateStackRequest]): Disjunction[Throwable, CreateStackResult] = {
    Disjunction.fromTryCatchNonFatal(client.createStack(conv(settings)))
  }

  /**
   * Updates a stack as specified in the template. After the call completes successfully, the stack update starts.
   * You can check the status of the stack via the DescribeStacks action.
   */
  def updateStack(
    settings: UpdateStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[UpdateStackSettings, UpdateStackRequest]): Disjunction[Throwable, UpdateStackResult] = {
    Disjunction.fromTryCatchNonFatal(client.updateStack(conv(settings)))
  }

  /**
   * Deletes a specified stack. Once the call completes successfully, stack deletion starts. Deleted stacks do not
   * show up in the DescribeStacks API if the deletion has been completed successfully.
   */
  def deleteStack(
    settings: DeleteStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DeleteStackSettings, DeleteStackRequest]): Disjunction[Throwable, DeleteStackResult] = {
    Disjunction.fromTryCatchNonFatal(client.deleteStack(conv(settings)))
  }

  /**
   * Returns the description for the specified stack; if no stack name was specified, then it returns the description
   * for all the stacks created.
   */
  def describeStack(
    settings: DescribeStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DescribeStackSettings, DescribeStacksRequest]): Disjunction[Throwable, DescribeStacksResult] = {
    Disjunction.fromTryCatchNonFatal(client.describeStacks(conv(settings)))
  }

  /**
   * Returns all stack related events for a specified stack in reverse chronological order.
   */
  def describeStackEvents(
    settings: DescribeStackEventsSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DescribeStackEventsSettings, DescribeStackEventsRequest]): Disjunction[Throwable, DescribeStackEventsResult] = {
    Disjunction.fromTryCatchNonFatal(client.describeStackEvents(conv(settings)))
  }

  def createStackEventGenerator(
    stackName: StackName,
    client: AmazonCloudFormation)(f: CloudFormationEvent => Unit): Unit = {
    import scala.collection.JavaConverters._
    var events: Seq[Event] = Nil

    def loop: Unit = {
      val stackStatus: String = describeStack(DescribeStackSettings(stackName), client)
        .bimap(_ => "STACK_DOES_NOT_EXIST", _.getStacks.get(0).getStackStatus).merge
      val stackEvents = describeStackEvents(DescribeStackEventsSettings(stackName), client)
        .bimap(t => DescribeStackEventsResponse(None, Option(t)), resp => DescribeStackEventsResponse(Option(resp), None)).merge
      val newEvents: Seq[Event] = stackEvents.response.map(_.getStackEvents.asScala.map(Event.fromStackEvent).reverse).getOrElse(Nil)
      if (List("FAILED", "COMPLETE", "STACK_DOES_NOT_EXIST").exists(state => stackStatus.contains(state))) {
        (newEvents diff events).foreach { event =>
          f(CloudFormationEvent(stackStatus, event))
        }
      } else {
        (newEvents diff events).foreach { event =>
          f(CloudFormationEvent(stackStatus, event))
        }
        events = newEvents
        Thread.sleep(500)
        loop
      }
    }

    loop
  }
}

object Event {
  def fromStackEvent(event: StackEvent): Event = {
    Event(
      event.getEventId,
      event.getLogicalResourceId,
      event.getPhysicalResourceId,
      event.getResourceType,
      event.getTimestamp.toString,
      event.getResourceStatus,
      event.getResourceStatusReason,
      event.getResourceProperties,
      event.getStackId,
      event.getStackName
    )
  }
}

final case class Event(
    eventId: String,
    logicalResourceId: String,
    physicalResourceId: String,
    resourceType: String,
    timestamp: String,
    resourceStatus: String,
    resourceStatusReason: String,
    resourceProperties: String,
    stackId: String,
    stackName: String)

final case class CloudFormationEvent(status: String, event: Event)
