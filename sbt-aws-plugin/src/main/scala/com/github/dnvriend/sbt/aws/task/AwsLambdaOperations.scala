package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.lambda._
import com.amazonaws.services.lambda.model._

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * add-permission                           | create-alias
 * create-event-source-mapping              | create-function
 * delete-alias                             | delete-event-source-mapping
 * delete-function                          | get-account-settings
 * get-alias                                | get-event-source-mapping
 * get-function                             | get-function-configuration
 * get-policy                               | invoke
 * invoke-async                             | list-aliases
 * list-event-source-mappings               | list-functions
 * list-tags                                | list-versions-by-function
 * publish-version                          | remove-permission
 * tag-resource                             | untag-resource
 * update-alias                             | update-event-source-mapping
 * update-function-code                     | update-function-configuration
 */

object AwsLambdaOperations {
  def client(): AWSLambda = {
    AWSLambdaClientBuilder.defaultClient()
  }

  def listFunctions(client: AWSLambda): List[FunctionConfiguration] = {
    client.listFunctions().getFunctions.asScala.toList
  }

  private def withGetFunctionRequest(f: GetFunctionRequest => Option[GetFunctionResult]): Option[GetFunctionResult] = {
    f(new GetFunctionRequest())
  }

  def getFunction(functionName: String, client: AWSLambda): Option[GetFunctionResult] = withGetFunctionRequest { req =>
    Try(client.getFunction(req.withFunctionName(functionName))).toOption
  }

  def findFunction(fqcn: String, client: AWSLambda): Option[FunctionConfiguration] = {
    println(s"Getting info for function: '$fqcn'")
    for {
      functions <- Try(client.listFunctions().getFunctions).toOption
      function <- functions.asScala.find(_.getHandler.startsWith(fqcn))
    } yield function
  }

  private def withInvokeRequest(f: InvokeRequest => InvokeResult): InvokeResult = {
    f(new InvokeRequest())
  }

  def invoke(functionName: String, payload: String, client: AWSLambda): InvokeResult = withInvokeRequest { req =>
    client.invoke(
      req
        .withFunctionName(functionName)
        .withPayload(payload)
    )
  }
}
