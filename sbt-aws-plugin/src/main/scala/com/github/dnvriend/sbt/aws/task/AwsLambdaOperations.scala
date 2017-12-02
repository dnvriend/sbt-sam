package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.lambda._
import com.amazonaws.services.lambda.model.FunctionConfiguration
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._

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


object LambdaFunction {
  implicit val fromFunctionConfiguration: Converter[FunctionConfiguration, LambdaFunction] =
    Converter.instance(fc => LambdaFunction(
      fc.getFunctionName,
      fc.getFunctionArn,
      fc.getRuntime,
      fc.getRole,
      fc.getHandler,
      fc.getCodeSize.longValue(),
      fc.getDescription,
      fc.getTimeout.longValue(),
      fc.getMemorySize.longValue(),
      fc.getLastModified,
      fc.getCodeSha256,
      fc.getVersion
    ))
}
case class LambdaFunction(FunctionName: String,
                           FunctionArn: String,
                           Runtime: String,
                           Role: String,
                           Handler: String,
                           CodeSize: Long,
                           Description: String,
                           Timeout: Long,
                           MemorySize: Long,
                           LastModified: String,
                           CodeSha256: String,
                           Version: String,
                         )

object AwsLambdaOperations {
  def client(cr: CredentialsAndRegion): AWSLambda = {
    AWSLambdaClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  def listFunctions(client: AWSLambda): List[LambdaFunction] = {
    client.listFunctions().getFunctions.asScala.toList.map(Converter[FunctionConfiguration, LambdaFunction])
  }
}
