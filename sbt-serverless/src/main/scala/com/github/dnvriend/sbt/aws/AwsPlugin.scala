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

import sbt.Keys._
import sbt.{AutoPlugin, Def, Defaults, PluginTrigger, State, _}

object AwsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  val autoImport = AwsPluginKeys

  lazy val startupTransition: State => State = { s: State =>
    "awsCognitoUserPools" :: "awsListTables" :: s
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    onLoad in Global := {
      val old = (onLoad in Global).value
      startupTransition compose old
    },

    // settings
    awsRegion := DEFAULT_REGION,
    awsProfile := DEFAULT_PROFILE,
    awsCognitoClientId := EMPTY,
    awsCognitoUserPoolId := EMPTY,
    awsCognitoUserName := EMPTY,
    awsCognitoPassword := EMPTY,

    // cognito tasks
    awsCognitoUserPools := {
      AwsOperations.getlistOfUserPools(awsRegion.value, awsProfile.value, streams.value.log)
    },
    awsCognitoUserPools := awsCognitoUserPools.keepAs(awsCognitoUserPools).value,

    awsCognitoUserPoolClients := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        AwsOperations.getListOfUserPoolClients(userPoolId, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsCognitoDeleteUserPoolClient := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        val clientId: String = AwsOperations.readInput("Enter clientId: ")
        AwsOperations.deleteUserPoolClient(userPoolId, clientId, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsCognitoCreateUserPoolClient := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        val clientName: String = AwsOperations.readInput("Enter client name: ")
        AwsOperations.createUserPoolClient(userPoolId, clientName, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsCognitoUsersForPool := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        AwsOperations.listUsers(userPoolId, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsCognitoDeleteUser := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        val username: String = AwsOperations.readInput("Enter username: ")
        AwsOperations.deleteUser(userPoolId, username, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsCognitoCreateUser := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        val username: String = AwsOperations.readInput("Enter username: ")
        AwsOperations.createUser(userPoolId, username, AWS_COGNITO_DEFAULT_USER_PASSWORD, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsCognitoConfirmUser := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsCognitoUserPools)((_, maybeUserPools) => {
        val strings = maybeUserPools.map(_.map(_.Name)).getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val userPools = awsCognitoUserPools.value
        val profile = awsProfile.value
        val region = awsRegion.value
        val log = streams.value.log
        val userPoolName = parser.parsed
        val userPoolId: String = userPools.find(_.Name == userPoolName).map(_.Id).getOrElse(sys.error(s"Could not find userpool for name '$userPoolName'"))
        val clientId: String = AwsOperations.readInput("Enter clientId: ")
        val username: String = AwsOperations.readInput("Enter username: ")
        val newPassword: String = AwsOperations.readInput("Enter new password: ")

        val session: String = AwsOperations.confirmUser(userPoolId, clientId, username, AWS_COGNITO_DEFAULT_USER_PASSWORD, region, profile, log)
        AwsOperations.respondToAuthChallenge(userPoolId, clientId, username, newPassword, session, region, profile, log)
      }
    }.evaluated,

    awsGetCognitoTokens := AwsAuthenticationService.run(
      awsProfile.value,
      awsCognitoClientId.value,
      awsCognitoUserPoolId.value,
      awsCognitoUserName.value,
      awsCognitoPassword.value,
      awsRegion.value,
      streams.value.log
    ),

    // dynamodb tasks
    awsListTables := {
      AwsOperations.listTables(awsRegion.value, awsProfile.value, streams.value.log)
    },
    awsListTables := awsListTables.keepAs(awsListTables).value,

    awsDescribeTable := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsListTables)((_, maybeTables) => {
        val strings = maybeTables.getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val tableName = parser.parsed
        AwsOperations.describeTable(tableName, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,

    awsDescribeTables := {
      val tables: List[String] = awsListTables.value
      AwsOperations.listAllTables(tables, awsRegion.value, awsProfile.value, streams.value.log)
    },
    awsDescribeTables := awsDescribeTables.keepAs(awsDescribeTables).value,

    awsScanTable := {
      import sbt.complete.DefaultParsers._
      val parser = Defaults.getForParser(awsListTables)((_, maybeTables) => {
        val strings = maybeTables.getOrElse(Nil)
        Space ~> StringBasic.examples(strings: _*)
      })
      Def.inputTask {
        val tableName = parser.parsed
        AwsOperations.scanTable(tableName, awsRegion.value, awsProfile.value, streams.value.log)
      }
    }.evaluated,
  )
}