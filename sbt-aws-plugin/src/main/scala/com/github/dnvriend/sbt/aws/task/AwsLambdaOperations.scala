package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.lambda._
import com.amazonaws.services.lambda.model._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Try }

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
    Try(client.listFunctions().getFunctions.asScala.toList).recoverWith {
      case t =>
        println(t.getMessage)
        Failure(t)
    }.getOrElse(Nil)
  }

  private def withGetFunctionRequest(f: GetFunctionRequest => Option[GetFunctionResult]): Option[GetFunctionResult] = {
    f(new GetFunctionRequest())
  }

  def getFunction(functionName: String, client: AWSLambda): Option[GetFunctionResult] = withGetFunctionRequest { req =>
    Try(client.getFunction(req.withFunctionName(functionName))).recoverWith {
      case t =>
        println(t.getMessage)
        Failure(t)
    }.toOption
  }

  def findFunction(fqcn: String, projectName: String, stage: String, client: AWSLambda): Option[FunctionConfiguration] = {
    def predicate(conf: FunctionConfiguration): Boolean = {
      val env = Option(conf.getEnvironment).map(_.getVariables.asScala)
      conf.getHandler.startsWith(s"$fqcn::handleRequest") &&
        env.get("PROJECT_NAME").contains(projectName) &&
        env.get("STAGE").contains(stage)
    }
    val result = for {
      functions <- Option(client.listFunctions().getFunctions)
      function <- functions.asScala.find(predicate)
    } yield function
    result
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
