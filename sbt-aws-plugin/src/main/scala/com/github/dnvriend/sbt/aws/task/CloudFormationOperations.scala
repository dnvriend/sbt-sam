package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cloudformation._
import com.amazonaws.services.cloudformation.model._
import com.github.dnvriend.ops.Converter
import play.api.libs.json.{ JsValue, Json }

import scala.collection.JavaConverters._
import scala.compat.Platform
import scalaz.{ Show, Disjunction }

object TemplateBody {
  implicit val toRequest: Converter[TemplateBody, ValidateTemplateRequest] =
    Converter.instance(template => new ValidateTemplateRequest().withTemplateBody(template.value))
  def fromJson(json: JsValue): TemplateBody = {
    TemplateBody(Json.prettyPrint(json))
  }
}

final case class TemplateBody(value: String) {
  require(value.nonEmpty, "template body should not be empty")
}

object CreateStackSettings {
  implicit val toRequest: Converter[CreateStackSettings, CreateStackRequest] =
    Converter.instance(settings => {
      new CreateStackRequest()
        .withStackName(settings.stackName.value)
        .withTemplateBody(settings.template.value)
    })
}

final case class StackName(value: String) {
  require(value.nonEmpty, "Stack name should not be empty")
}

final case class ChangeSetName(value: String) {
  require(value.nonEmpty, "change set name should not be empty")
}

final case class CreateStackSettings(template: TemplateBody, stackName: StackName)

object CreateStackResponse {
  val zero = CreateStackResponse(None, None)
}

final case class CreateStackResponse(response: Option[CreateStackResult], failure: Option[Throwable])

object UpdateStackSettings {
  implicit val toRequest: Converter[UpdateStackSettings, UpdateStackRequest] =
    Converter.instance(settings => {
      new UpdateStackRequest()
        .withStackName(settings.stackName.value)
        .withTemplateBody(settings.template.value)
    })
}

object CreateChangeSetSettings {
  implicit val toRequest: Converter[CreateChangeSetSettings, CreateChangeSetRequest] =
    Converter.instance(settings ⇒ {
      new CreateChangeSetRequest()
        .withStackName(settings.stackName.value)
        .withTemplateBody(settings.template.value)
        .withChangeSetName(settings.changeSetName.value)
        .withCapabilities(settings.capability)
    })
}

final case class CreateChangeSetSettings(template: TemplateBody, stackName: StackName, changeSetName: ChangeSetName, capability: Capability)

final case class UpdateStackSettings(template: TemplateBody, stackName: StackName)

object DeleteStackSettings {
  implicit val toRequest: Converter[DeleteStackSettings, DeleteStackRequest] =
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
  implicit val toRequest: Converter[DescribeStackSettings, DescribeStacksRequest] =
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
  implicit val toRequest: Converter[DescribeStackEventsSettings, DescribeStackEventsRequest] =
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

object DescribeStackResourcesSettings {
  implicit val toRequest: Converter[DescribeStackResourcesSettings, DescribeStackResourcesRequest] =
    Converter.instance(settings => {
      new DescribeStackResourcesRequest()
        .withStackName(settings.stackName.value)
    })
}
final case class DescribeStackResourcesSettings(stackName: StackName)

final case class DescribeStackResourcesResponse(result: Option[DescribeStackResourcesResult], failure: Option[Throwable])

object DescribeChangeSetSettings {
  implicit val toRequest: Converter[DescribeChangeSetSettings, DescribeChangeSetRequest] =
    Converter.instance(settings => {
      new DescribeChangeSetRequest()
        .withStackName(settings.stackName.value)
        .withChangeSetName(settings.changeSetName.value)
    })
}
final case class DescribeChangeSetSettings(stackName: StackName, changeSetName: ChangeSetName)

final case class ServiceEndpoint(value: String)
object SamStack {
  implicit val show: Show[SamStack] = Show.shows(model => {
    import model._
    s"""
       |====================
       |Sam's State:
       |====================
       |Name: ${stack.getStackName}
       |Description: ${Option(stack.getDescription).filter(_ != "null").getOrElse("No description")}
       |Status: ${stack.getStackStatus}
       |Status reason: ${Option(stack.getStackStatusReason).filter(_ != "null").getOrElse("No status reason")}
       |Last updated: ${stack.getLastUpdatedTime}
       |===================
       |ServiceEndpoint: ${serviceEndpoint.map(_.value).getOrElse("No endpoint")}
       |===================
     """.stripMargin
  })
  def fromStack(stack: Stack): SamStack = {
    val outputs = stack.getOutputs.asScala.toList
    val serviceEndpoint: Option[ServiceEndpoint] =
      outputs.find(_.getOutputKey == "ServiceEndpoint").map(o => ServiceEndpoint(o.getOutputValue))
    SamStack(
      serviceEndpoint,
      stack)
  }
}
final case class SamStack(serviceEndpoint: Option[ServiceEndpoint], stack: Stack)

object CloudFormationOperations extends AwsProgressListenerOps {
  def client(): AmazonCloudFormation = {
    AmazonCloudFormationClientBuilder.defaultClient()
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
    println("====> Cloudformation stack string: " + settings.template.value)
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
   * Creates a list of changes that will be applied to a stack so that you can review the changes
   * before executing them. You can create a change set for a stack that doesn't exist or an existing
   * stack.
   */
  def createChangeSet(
    settings: CreateChangeSetSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[CreateChangeSetSettings, CreateChangeSetRequest]): Disjunction[Throwable, CreateChangeSetResult] = {
    Disjunction.fromTryCatchNonFatal(client.createChangeSet(conv(settings)))
  }

  /**
   * Returns the inputs for the change set and a list of changes that AWS CloudFormation will make if you execute the change set.
   */
  def describeChangeSet(
    settings: DescribeChangeSetSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DescribeChangeSetSettings, DescribeChangeSetRequest]): Disjunction[Throwable, DescribeChangeSetResult] = {
    Disjunction.fromTryCatchNonFatal(client.describeChangeSet(conv(settings)))
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
   * Returns the CloudFormation Stack
   */
  def getStack(
    settings: DescribeStackSettings,
    client: AmazonCloudFormation): Option[Stack] = {
    describeStack(settings, client).toOption.flatMap(_.getStacks.asScala.headOption)
  }

  /**
   * Returns all stack related events for a specified stack in reverse chronological order.
   */
  def describeStackEvents(
    settings: DescribeStackEventsSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DescribeStackEventsSettings, DescribeStackEventsRequest]): Disjunction[Throwable, DescribeStackEventsResult] = {
    Disjunction.fromTryCatchNonFatal(client.describeStackEvents(conv(settings)))
  }

  /**
   * Returns AWS resource descriptions for running and deleted stacks. If StackName is specified, all the associated
   * resources that are part of the stack are returned.
   */
  def describeStackResources(
    settings: DescribeStackResourcesSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DescribeStackResourcesSettings, DescribeStackResourcesRequest]): Disjunction[Throwable, DescribeStackResourcesResult] = {
    Disjunction.fromTryCatchNonFatal(client.describeStackResources(conv(settings)))
  }

  /**
   * Continuously polls cloud formation and publishes events until finished
   */
  def waitForCloudFormation(
    stackName: StackName,
    client: AmazonCloudFormation)(f: CloudFormationEvent => Unit): Unit = {
    import scalaz.Scalaz._

    val now: Long = Platform.currentTime
    var events: Set[Event] = Set.empty
    def publishEvents(stackStatus: String, newEvents: Set[Event]): Unit = {
      val event = CloudFormationEvent.apply _ curried stackStatus
      val ys = ((newEvents diff events) map Option.apply map event).toList.toNel
      ys.getOrElse(event(None).wrapNel) foreach { cloudFormationEvent =>
        f(cloudFormationEvent)
      }
    }
    def loop: Unit = {
      val stackStatus: String = describeStack(DescribeStackSettings(stackName), client)
        .bimap(_ => "STACK_DOES_NOT_EXIST", _.getStacks.get(0).getStackStatus).merge
      val stackEvents = describeStackEvents(DescribeStackEventsSettings(stackName), client)
        .bimap(t => DescribeStackEventsResponse(None, Option(t)), resp => DescribeStackEventsResponse(Option(resp), None)).merge
      val newEvents: Set[Event] = {
        stackEvents.response.map(_.getStackEvents.asScala.filter(_.getTimestamp.getTime >= now).map(Event.fromStackEvent).toSet)
          .getOrElse(Set.empty)
      }
      if (List("FAILED", "COMPLETE", "STACK_DOES_NOT_EXIST").exists(state => stackStatus.contains(state))) {
        publishEvents(stackStatus, newEvents)
      } else {
        publishEvents(stackStatus, newEvents)
        events = newEvents
        Thread.sleep(500)
        loop
      }
    }

    loop
  }

  /**
   * Continuously polls cloud formation until the change set becomes available
   */
  def waitForChangeSetAvailable(
    stackName: StackName,
    changeSetName: ChangeSetName,
    client: AmazonCloudFormation)(f: ChangeSetEvent => Unit): ChangeSetEvent = {
    def publishEvent(event: ChangeSetEvent): ChangeSetEvent = {
      f(event)
      event
    }
    def loop: ChangeSetEvent = {
      val event = describeChangeSet(DescribeChangeSetSettings(stackName, changeSetName), client)
        .bimap(
          error => ChangeSetEvent.failed(stackName.value, changeSetName.value, error),
          result => ChangeSetEvent(stackName.value, changeSetName.value, result.getStatus, result.getExecutionStatus, result.getStatusReason)).merge

      if (List("FAILED", "COMPLETE").exists(state => event.status.contains(state))) {
        publishEvent(event)
      } else {
        publishEvent(event)
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
      event.getResourceStatus,
      event.getResourceStatusReason,
      event.getResourceProperties,
      event.getStackId,
      event.getStackName,
      event.getTimestamp.toString,
      event.getTimestamp.getTime
    )
  }
}

final case class Event(
    eventId: String,
    logicalResourceId: String,
    physicalResourceId: String,
    resourceType: String,
    resourceStatus: String,
    resourceStatusReason: String,
    resourceProperties: String,
    stackId: String,
    stackName: String,
    timestampAsString: String,
    timestampAsLong: Long)

final case class CloudFormationEvent(status: String, event: Option[Event])

object ChangeSetEvent {
  def failed(stackName: String, changeSetName: String, throwable: Throwable): ChangeSetEvent = {
    ChangeSetEvent(stackName, changeSetName, "FAILED", "FAILED", throwable.getMessage)
  }
}
final case class ChangeSetEvent(
    stackName: String,
    changeSetName: String,
    status: String,
    executionStatus: String,
    statusReason: String)