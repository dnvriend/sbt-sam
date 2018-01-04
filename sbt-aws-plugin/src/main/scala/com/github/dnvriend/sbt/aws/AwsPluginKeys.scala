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
import com.amazonaws.services.codebuild.AWSCodeBuild
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{ FunctionConfiguration, GetFunctionResult, InvokeResult }
import com.amazonaws.services.logs.AWSLogs
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.xray.AWSXRay
import com.github.dnvriend.sbt.aws.domain.IAMDomain.CredentialsRegionAndUser
import com.github.dnvriend.sbt.aws.task._
import sbt._

object AwsPluginKeys {
  // clients
  lazy val clientAwsLambda = SettingKey[AWSLambda]("Returns the AwsLambda client")
  lazy val clientApiGateway = SettingKey[AmazonApiGateway]("Returns the ApiGateway client")
  lazy val clientDynamoDb = SettingKey[AmazonDynamoDB]("Returns the DynamoDb client")
  lazy val clientS3 = SettingKey[AmazonS3]("Returns the s3 client")
  lazy val clientKinesis = SettingKey[AmazonKinesis]("Returns the kinesis client")
  lazy val clientSns = SettingKey[AmazonSNS]("Returns the simple notification service client")
  lazy val clientCloudWatch = SettingKey[AmazonCloudWatch]("Returns the amazon cloud watch client")
  lazy val clientAwsLogs = SettingKey[AWSLogs]("Returns the amazon cloud watch logs client")
  lazy val clientIam = SettingKey[AmazonIdentityManagement]("Returns the amazon identity and access management (IAM client")
  lazy val clientCloudFormation = SettingKey[AmazonCloudFormation]("Returns the amazon cloud formation client")
  lazy val clientCodeBuild = SettingKey[AWSCodeBuild]("AWS CodeBuild is a fully managed build service in the cloud. AWS CodeBuild compiles your source code, runs unit tests, and produces artifacts that are ready to deploy. AWS CodeBuild eliminates the need to provision, manage, and scale your own build servers")
  lazy val clientXRay = SettingKey[AWSXRay]("AWS X-Ray provides APIs for managing debug traces and retrieving service maps and other data created by processing those traces")
  lazy val clientCognito = SettingKey[AWSCognitoIdentityProvider]("Returns the cognito identity provider client")
  lazy val clientKinesisFirehose = SettingKey[AmazonKinesisFirehose]("Returns the amazon kinesis firehose client")

  // lambda tasks
  lazy val lambdaListFunctions = taskKey[List[FunctionConfiguration]]("Returns a list of Lambda functions")
  lazy val lambdaGetFunction = inputKey[Option[GetFunctionResult]]("Returns the configuration information of the Lambda function")
  lazy val lambdaInvoke = inputKey[InvokeResult]("Invokes a specific Lambda function")
  lazy val lambdaMetrics = taskKey[LambdaMetrics]("Get metrics for all lambdas")

  lazy val lambdaLog = inputKey[Unit]("Shows log of specific Lambda function")

  // cloud formation tasks
  lazy val cfDescribeStack = inputKey[DescribeStackResponse]("Returns the description for the specified stack; if no stack name was specified, then it returns the description for all the stacks created")
  lazy val cfDescribeStackEvents = inputKey[DescribeStackEventsResponse]("Returns all stack related events for a specified stack in reverse chronological order")
  lazy val cfDeleteStack = inputKey[DeleteStackResponse]("Deletes a specified stack. Once the call completes successfully, stack deletion starts. Deleted stacks do not show up in the DescribeStacks API if the deletion has been completed successfully")

  // iam tasks
  lazy val iamUserInfo = settingKey[AmazonUser]("Returns the current Amazon user and details")
  lazy val iamCredentialsRegionAndUser = taskKey[CredentialsRegionAndUser]("Returns the current user")
  lazy val whoAmI = taskKey[Unit]("Shows the current region and credentials in use")

  // code build tasks
  lazy val cbGenerateBuildSpec = taskKey[File]("Generates a buildspec.yaml file in the root project directory")
}
