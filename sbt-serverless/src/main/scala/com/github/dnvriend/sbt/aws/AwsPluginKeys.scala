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

import sbt._

object AwsPluginKeys {
  // aws init
  lazy val awsInit = TaskKey[Unit]("aws-init", "Initializes all list tasks")
  // cognito
  lazy val awsCognitoUserPoolId = settingKey[String]("The Cognito User Pool Id to use")
  lazy val awsCognitoClientId = settingKey[String]("The Cognito Client Id to use")
  lazy val awsCognitoUserName = settingKey[String]("The Cognito User Name to use")
  lazy val awsCognitoPassword = settingKey[String]("The Cognito Password Name to use")
  // aim account
  lazy val awsAccountId = settingKey[String]("The AWS numeric account ID")
  // profile and region
  lazy val awsProfile = settingKey[String]("The AWS profile to use")
  lazy val awsRegion = settingKey[String]("The region to deploy the service to")

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
}
