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

package com.github.dnvriend.sbt

package object sam {
  final val SAM_PLUGIN_WORK_DIR = ".sam"
  final val DEFAULT_STAGE = "dev"
  final val SAM_PROJECT_BASE_URL = "SAM_PROJECT_BASE_URL"
  final val ID_TOKEN = "ID_TOKEN"

  object Filters {
    val itFilter: String => Boolean = (_: String) endsWith "IntegrationTest"
    val unitFilter: String => Boolean = (name: String) => (name endsWith "Test") && !itFilter(name)
  }

  def createCloudFormationTemplate(resources: String, description: String = ""): String = {
    s"""
       |{
       |  "AWSTemplateFormatVersion": "2010-09-09",
       |  "Description": "$description",
       |  "Resources": {
       |     $resources
       |  }
       |}
    """.stripMargin
  }


  object Cognito {

    case class AdminCreateUserConfig(allowAdminCreateUserOnly: Boolean,
                                     unusedAccountValidityDays: Int,
                                    )

    case class PasswordPolicy(minimumLength: Int,
                              requireLowercase: Boolean,
                              requireNumbers: Boolean,
                              requireSymbols: Boolean,
                              requireUppercase: Boolean,
                             )

    case class Policies(passwordPolicy: PasswordPolicy)

    case class UserPool(name: String,
                        adminCreateUserConfig: AdminCreateUserConfig,
                        policies: Policies,
                        configName: String = ""
                       )

  }

  object DynamoDb {

    case class HashKey(name: String, `type`: String = "S")

    case class SortKey(name: String, `type`: String = "S")

    case class Table(name: String, hashKey: HashKey, sortKey: Option[SortKey] = None, stream: Option[String] = None, rcu: Int = 1, wcu: Int = 1, configName: String = "")

  }

  object Lambda {

    trait LambdaHandler

    case class HttpHandler(fqcn: String,
                           simpleClassName: String,
                           stage: String = "",
                           path: String = "/",
                           method: String = "get",
                           authorization: Boolean = false,
                           name: String = "",
                           memorySize: Int = 1024,
                           timeout: Int = 300,
                           description: String = "",
                          ) extends LambdaHandler

    case class DynamoHandler(fqcn: String = "",
                             simpleClassName: String = "",
                             stage: String = "",
                             tableName: String = "",
                             batchSize: Int = 100,
                             startingPosition: String = "LATEST",
                             enabled: Boolean = true,
                             name: String = "",
                             memorySize: Int = 1024,
                             timeout: Int = 300,
                             description: String = "",
                             streamArn: Option[String] = None,
                            ) extends LambdaHandler

  }

  case class ProjectConfiguration(lambdas: List[Lambda.LambdaHandler], tables: List[DynamoDb.Table], userPools: List[Cognito.UserPool])

  object Serverless {

    case class LambdaInfo(name: String, arn: String)

    case class Info(slsVersion: String, endpoint: Option[String], lambdas: List[LambdaInfo], slsBucketName: Option[String])

    case class Options(stage: String, profile: String, region: String) {
      def cliOptions: String = s"--aws-profile $profile --stage $stage --region $region"
    }

  }

}
