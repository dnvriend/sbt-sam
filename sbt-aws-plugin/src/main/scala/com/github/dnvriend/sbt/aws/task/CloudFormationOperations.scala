package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cloudformation._
import com.amazonaws.services.cloudformation.model._
import com.github.dnvriend.ops.Converter

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

object DeleteStackSettings {
  implicit val toDeleteStackRequest: Converter[DeleteStackSettings, DeleteStackRequest] =
    Converter.instance(settings => {
      new DeleteStackRequest()
        .withStackName(settings.stackName.value)
    })
}
final case class DeleteStackSettings(stackName: StackName)

object DescribeStackSettings {
  implicit val toDescribeStacksRequest: Converter[DescribeStackSettings, DescribeStacksRequest] =
    Converter.instance(settings => {
      new DescribeStacksRequest()
        .withStackName(settings.stackName.value)
    })
}
final case class DescribeStackSettings(stackName: StackName)

/**
 * cancel-update-stack                      | continue-update-rollback
 * create-change-set                        | create-stack
 * create-stack-instances                   | create-stack-set
 * delete-change-set                        | delete-stack
 * delete-stack-instances                   | delete-stack-set
 * describe-account-limits                  | describe-change-set
 * describe-stack-events                    | describe-stack-instance
 * describe-stack-resource                  | describe-stack-resources
 * describe-stack-set                       | describe-stack-set-operation
 * describe-stacks                          | estimate-template-cost
 * execute-change-set                       | get-stack-policy
 * get-template                             | get-template-summary
 * list-change-sets                         | list-exports
 * list-imports                             | list-stack-instances
 * list-stack-resources                     | list-stack-set-operation-results
 * list-stack-set-operations                | list-stack-sets
 * list-stacks                              | set-stack-policy
 * signal-resource                          | stop-stack-set-operation
 * update-stack                             | update-stack-set
 * update-termination-protection            | validate-template
 * package                                  | deploy
 */
object CloudFormationOperations {
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
    client: AmazonCloudFormation)(implicit conv: Converter[TemplateBody, ValidateTemplateRequest]): ValidateTemplateResult = {
    client.validateTemplate(conv(templateBody))
  }

  /**
   * Creates a stack as specified in the template. After the call completes successfully, the stack creation starts. You can check the status of the stack via the DescribeStacks API.
   */
  def createStack(
    settings: CreateStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[CreateStackSettings, CreateStackRequest]): CreateStackResult = {
    client.createStack(conv(settings))
  }

  /**
   * Deletes a specified stack. Once the call completes successfully, stack deletion starts. Deleted stacks do not show up in the DescribeStacks API if the deletion has been completed successfully.
   */
  def deleteStack(
    settings: DeleteStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DeleteStackSettings, DeleteStackRequest]): DeleteStackResult = {
    client.deleteStack(conv(settings))
  }

  /**
   * Returns the description for the specified stack; if no stack name was specified, then it returns the description for all the stacks created.
   */
  def describeStack(
    settings: DescribeStackSettings,
    client: AmazonCloudFormation)(implicit conv: Converter[DescribeStackSettings, DescribeStacksRequest]): DescribeStacksResult = {
    client.describeStacks(conv(settings))
  }
}
