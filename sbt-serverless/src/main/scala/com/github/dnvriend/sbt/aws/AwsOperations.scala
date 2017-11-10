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

import play.api.libs.json.Json
import sbt.{ Logger, SimpleReader }

import scala.sys.process.Process

object AwsOperations {
  def cliOptions(profile: String, stage: String, region: String): String = {
    s"--aws-profile $profile --stage $stage --region $region"
  }

  def scanTable(tableName: String, region: String, profile: String, log: Logger): Unit = {
    log.info(s"Scanning DynamoDB table '$tableName' for profile: '$profile', region: '$region'")
    val json = Process(s"aws dynamodb scan --table-name $tableName --region $region --profile $profile --output table").!!
    println(json)
  }

  def listAllTables(tables: List[String], region: String, profile: String, log: Logger): List[Dynamo.Table] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    import scala.concurrent.duration._
    val requests = Future.sequence(tables.map { tableName => Future(describeTable(tableName, region, profile, log)) })
    Await.result(requests, 5.minutes)
  }

  def listTables(region: String, profile: String, log: Logger): List[String] = {
    log.info(s"Getting list of DynamoDB tables for profile: '$profile', region: '$region'")
    val json = Process(s"aws dynamodb list-tables --page-size 100 --max-items 100 --region $region --profile $profile").!!
    val ast = Json.parse(json)
    (ast \ "TableNames").as[List[String]]
  }

  def describeTable(tableName: String, region: String, profile: String, log: Logger): Dynamo.Table = {
    import Dynamo._
    log.info(s"Getting information about table: '$tableName' for profile: '$profile', region: '$region'")
    val json = Process(s"aws dynamodb describe-table --table-name $tableName --region $region --profile $profile").!!
    val ast = Json.parse(json)
    val table = (ast \ "Table").get
    val arn = (table \ "TableArn").as[String]
    val name = (table \ "TableName").as[String]
    val status = (table \ "TableStatus").as[String]
    val itemCount = (table \ "ItemCount").as[Long]
    val sizeInBytes = (table \ "TableSizeBytes").as[Long]
    val maybeStream = (table \ "StreamSpecification").toOption
    val maybeStreamArn = (table \ "LatestStreamArn").toOption
    val streamArn = maybeStreamArn.flatMap { jsonArn =>
      maybeStream.map { json =>
        val viewType = (json \ "StreamViewType").as[String]
        val streamEnabled = (json \ "StreamEnabled").as[Boolean]
        val streamArn = jsonArn.as[String]
        Stream(streamArn, viewType, streamEnabled)
      }
    }

    Table(arn, name, status, itemCount, sizeInBytes, streamArn)
  }

  def deleteUser(userPoolId: String, username: String, region: String, profile: String, log: Logger): Unit = {
    log.info(s"Deleting user '$username' from userpool: '$userPoolId', region '$region', profile: '$profile'")
    Process(s"aws cognito-idp admin-delete-user --user-pool-id $userPoolId --username $username --region $region --profile $profile") ! log
  }

  def createUser(userPoolId: String, username: String, temporaryPassword: String, region: String, profile: String, log: Logger): Unit = {
    log.info(s"Creating user '$username' from userpool: '$userPoolId', region '$region', profile: '$profile'")
    Process(s"aws cognito-idp admin-create-user --user-pool-id $userPoolId --username $username --temporary-password $temporaryPassword --region $region --profile $profile") ! log
  }

  def confirmUser(userPoolId: String, clientId: String, username: String, password: String, region: String, profile: String, log: Logger): String = {
    log.info(s"Confirming user: '$username' for userpool: '$userPoolId', region: '$region', profile: '$profile'")
    val json = Process(s"aws cognito-idp admin-initiate-auth --user-pool-id $userPoolId --client-id $clientId --auth-parameters USERNAME=$username,PASSWORD=$password --auth-flow ADMIN_NO_SRP_AUTH --region $region --profile $profile").!!
    val ast = Json.parse(json)
    (ast \ "Session").as[String]
  }

  def respondToAuthChallenge(userPoolId: String, clientId: String, username: String, newpassword: String, session: String, region: String, profile: String, log: Logger): Unit = {
    log.info(s"Responding to auth challenge for region: '$region', profile: '$profile'")
    val str = s"aws cognito-idp admin-respond-to-auth-challenge --user-pool-id $userPoolId --client-id $clientId --challenge-name NEW_PASSWORD_REQUIRED --challenge-responses NEW_PASSWORD=$newpassword,USERNAME=$username --session $session"
    println(str)
    val json: String = Process(str).!!
    println(json)
  }

  def getListOfUserPoolClients(userPoolId: String, region: String, profile: String, log: Logger): List[Cognito.UserPoolClient] = {
    log.info(s"Getting list of user pools clients for profile: '$profile', region: '$region'")
    val json = Process(s"aws cognito-idp list-user-pool-clients --user-pool-id $userPoolId --max-results 50 --region $region --profile $profile").!!
    val ast = Json.parse(json)
    (ast \ "UserPoolClients").as[List[Cognito.UserPoolClient]]
  }

  def deleteUserPoolClient(userPoolId: String, clientId: String, region: String, profile: String, log: Logger): Unit = {
    log.info(s"Deleting user pool client: '$clientId' from user pool: '$userPoolId', region: '$region', profile: '$profile'")
    Process(s"aws cognito-idp delete-user-pool-client --user-pool-id $userPoolId --client-id $clientId --region $region --profile $profile") ! log
  }

  def createUserPoolClient(userPoolId: String, clientName: String, region: String, profile: String, log: Logger): Unit = {
    log.info(s"Creating user pool client: '$clientName' for user pool: '$userPoolId', region: '$region', profile: '$profile'")
    val json = Process(s"aws cognito-idp create-user-pool-client --client-name $clientName --user-pool-id $userPoolId --explicit-auth-flows ADMIN_NO_SRP_AUTH --region $region --profile $profile").!!
  }

  def adminInitiateAuth(userPoolId: String, clientId: String, username: String, password: String, region: String, profile: String, log: Logger): Unit = {
    val json = Process(s"aws cognito-idp admin-initiate-auth --user-pool-id $userPoolId --client-id $clientId --auth-flow ADMIN_NO_SRP_AUTH --auth-parameters USERNAME=$username,PASSWORD=$password --region $region --profile $profile").!!
  }

  def getlistOfUserPools(region: String, profile: String, log: Logger): List[Cognito.UserPool] = {
    log.info(s"Getting list of user pools for profile: '$profile', region: '$region'")
    val json = Process(s"aws cognito-idp list-user-pools --max-results 50 --region $region --profile $profile").!!
    val ast = Json.parse(json)
    (ast \ "UserPools").as[List[Cognito.UserPool]]
  }

  def listUsers(userPoolId: String, region: String, profile: String, log: Logger): List[Cognito.User] = {
    val json = Process(s"aws cognito-idp list-users --user-pool-id $userPoolId --region $region --profile $profile").!!
    val ast = Json.parse(json)
    val users = (ast \ "Users").as[List[Cognito.User]]

    users.foreach { user =>
      log.info(
        s"""=====================================
           |User: ${user.Username}
           |=====================================
           |Username: ${user.Username}
           |Status: ${user.UserStatus}
           |Enabled: ${user.Enabled}
        """.stripMargin)
    }

    users
  }

  def readInput(prompt: String): String = {
    SimpleReader.readLine(s"$prompt\n") getOrElse {
      val badInputMessage = "Unable to read input"
      val updatedPrompt = if (prompt.startsWith(badInputMessage)) prompt else s"$badInputMessage\n$prompt"
      readInput(updatedPrompt)
    }
  }
}