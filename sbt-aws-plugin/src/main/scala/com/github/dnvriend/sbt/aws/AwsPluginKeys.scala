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
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{ FunctionConfiguration, GetFunctionResult, InvokeResult }
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.sns.AmazonSNS
import com.github.dnvriend.sbt.aws.task.{ CredentialsAndRegion, LambdaMetrics }
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
  lazy val credentialsAndRegion = taskKey[CredentialsAndRegion]("Returns the aws credentials provider and region")

  // clients
  lazy val clientAwsLambda = TaskKey[AWSLambda]("Returns the AwsLambda client")
  lazy val clientApiGateway = TaskKey[AmazonApiGateway]("Returns the ApiGateway client")
  lazy val clientDynamoDb = TaskKey[AmazonDynamoDB]("Returns the DynamoDb client")
  lazy val clientS3 = TaskKey[AmazonS3]("Returns the s3 client")
  lazy val clientKinesis = TaskKey[AmazonKinesis]("Returns the kinesis client")
  lazy val clientSns = TaskKey[AmazonSNS]("Returns the simple notification service client")
  lazy val clientCloudWatch = TaskKey[AmazonCloudWatch]("Returns the amazon cloud watch client")

  // cognito tasks
  lazy val awsGetCognitoTokens = taskKey[Option[Cognito.AuthTokens]]("Get authentication tokens from AWS Cognito")
  lazy val awsCognitoUserPools = taskKey[List[Cognito.UserPool]]("Returns the user pools")
  lazy val awsCognitoUserPoolClients = inputKey[List[Cognito.UserPoolClient]]("Returns the user pools clients")
  lazy val awsCognitoDeleteUserPoolClient = inputKey[Unit]("Delete a user pool client")
  lazy val awsCognitoCreateUserPoolClient = inputKey[Unit]("Create a user pool client")
  lazy val awsCognitoUsersForPool = inputKey[List[Cognito.User]]("Lists the users in the Amazon Cognito user pool")
  lazy val awsCognitoCreateUser = inputKey[Unit]("Creates and confirms a new cognito user for a given user pool")
  lazy val awsCognitoConfirmUser = inputKey[Unit]("Confirm a cognito user for a given user pool")
  lazy val awsCognitoDeleteUser = inputKey[Unit]("Deletes a cognito user from a given user pool")

  // dynamodb tasks
  lazy val awsListTables = taskKey[List[String]]("Returns a list of table names associated with the current profile")
  lazy val awsDescribeTable = inputKey[Dynamo.Table]("Returns information about the table")
  lazy val awsDescribeTables = taskKey[List[Dynamo.Table]]("Returns information about all tables")
  lazy val awsScanTable = inputKey[Unit]("Shows table entries")

  // lambda tasks
  lazy val lambdaListFunctions = taskKey[List[FunctionConfiguration]]("Returns a list of Lambda functions")
  lazy val lambdaGetFunction = inputKey[GetFunctionResult]("Returns the configuration information of the Lambda function")
  lazy val lambdaInvoke = inputKey[InvokeResult]("Invokes a specific Lambda function")
  lazy val lambdaMetrics = taskKey[LambdaMetrics]("Get metrics for all lambdas")

  lazy val lambdaLog = inputKey[Unit]("Shows log of specific Lambda function")
}
