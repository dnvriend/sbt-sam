// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.sbt.aws

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{ FunctionConfiguration, GetFunctionResult, InvokeResult }
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.sns.AmazonSNS
import com.github.dnvriend.sbt.aws.task._
import sbt._

object AwsPluginKeys {
  // aim account
  lazy val awsAccountId = settingKey[String]("The AWS numeric account ID")
  // profile and region
  lazy val awsProfile = settingKey[String]("The AWS profile to use")
  lazy val awsRegion = settingKey[String]("The region to deploy the service to")
  lazy val awsAccessKeyId = settingKey[String]("The AWS access key to use")
  lazy val awsSecretAccessKey = settingKey[String]("The AWS secret access key to use")
  lazy val awsClientId = settingKey[String]("AWS client Id")

  // credentials tasks
  lazy val credentialsAndRegion = SettingKey[CredentialsAndRegion]("Returns the aws credentials provider and region")

  // clients
  lazy val clientAwsLambda = SettingKey[AWSLambda]("Returns the AwsLambda client")
  lazy val clientApiGateway = SettingKey[AmazonApiGateway]("Returns the ApiGateway client")
  lazy val clientDynamoDb = SettingKey[AmazonDynamoDB]("Returns the DynamoDb client")
  lazy val clientS3 = SettingKey[AmazonS3]("Returns the s3 client")
  lazy val clientKinesis = SettingKey[AmazonKinesis]("Returns the kinesis client")
  lazy val clientSns = SettingKey[AmazonSNS]("Returns the simple notification service client")
  lazy val clientCloudWatch = SettingKey[AmazonCloudWatch]("Returns the amazon cloud watch client")
  lazy val clientIam = SettingKey[AmazonIdentityManagement]("Returns the amazon identity and access management (IAM client")
  lazy val clientCloudFormation = SettingKey[AmazonCloudFormation]("Returns the amazon cloud formation client")
  lazy val clientCognito = SettingKey[AWSCognitoIdentityProvider]("Returns the cognito identity provider client")

  // lambda tasks
  lazy val lambdaListFunctions = taskKey[List[FunctionConfiguration]]("Returns a list of Lambda functions")
  lazy val lambdaGetFunction = inputKey[GetFunctionResult]("Returns the configuration information of the Lambda function")
  lazy val lambdaInvoke = inputKey[InvokeResult]("Invokes a specific Lambda function")
  lazy val lambdaMetrics = taskKey[LambdaMetrics]("Get metrics for all lambdas")

  lazy val lambdaLog = inputKey[Unit]("Shows log of specific Lambda function")

  // cloud formation tasks
  lazy val cfDescribeStack = inputKey[DescribeStackResponse]("Returns the description for the specified stack; if no stack name was specified, then it returns the description for all the stacks created")
  lazy val cfDescribeStackEvents = inputKey[DescribeStackEventsResponse]("Returns all stack related events for a specified stack in reverse chronological order")
  lazy val cfDeleteStack = inputKey[DeleteStackResponse]("Deletes a specified stack. Once the call completes successfully, stack deletion starts. Deleted stacks do not show up in the DescribeStacks API if the deletion has been completed successfully")

  // iam tasks
  lazy val iamUserInfo = settingKey[AmazonUser]("Returns the current Amazon user and details")

  // cognito tasks
  lazy val usersToCreate = settingKey[List[CognitoUserDetails]]("List of username and password for user accounts to create")
  lazy val createValidUsers = taskKey[List[ValidUser]]("Creates and authenticates all users specified in usersToCreate and returns the successfully validated users, , if no users are specified does nothing,")
}
