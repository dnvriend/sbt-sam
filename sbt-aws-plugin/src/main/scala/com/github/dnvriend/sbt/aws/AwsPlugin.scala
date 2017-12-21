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

import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.aws.task._
import com.github.dnvriend.sbt.aws.domain._
import sbt.complete.DefaultParsers._
import sbt._
import sbt.Keys._

import scalaz.std.AllInstances._

import scalaz.Disjunction

object AwsPlugin extends AutoPlugin with AllOps {

  override def trigger: PluginTrigger = allRequirements

  val autoImport = AwsPluginKeys

  import autoImport._

  override def projectSettings = Seq(
    clientAwsLambda := AwsLambdaOperations.client(),
    clientApiGateway := ApiGatewayOperations.client(),
    clientDynamoDb := DynamoDbOperations.client(),
    clientS3 := S3Operations.client(),
    clientKinesis := KinesisOperations.client(),
    clientSns := SNSOperations.client(),
    clientCloudWatch := CloudWatchOperations.client(),
    clientIam := IamOperations.client(),
    clientCloudFormation := CloudFormationOperations.client(),
    clientCodeBuild := CodeBuildOperations.client(),
    clientXRay := XRayOperations.client(),
    clientCognito := AwsCognitoIdpOperations.client(),

    // cognito users
    usersToCreate := List(),

    // iam operations
    iamUserInfo := IamOperations.getUser(clientIam.value),
    iamCredentialsRegionAndUser := IamOperations.getAwsCredentialsAndUser(clientIam.value).getOrFail(),
    iamCredentialsRegionAndUser := (iamCredentialsRegionAndUser keepAs iamCredentialsRegionAndUser).value,

    whoAmI := {
      val log = streams.value.log
      val creds: IAMDomain.CredentialsRegionAndUser = iamCredentialsRegionAndUser.value
      log.info(
        s"""
           |===================================
           |Using the following AWS credentials
           |===================================
           |* ProfileLocation: ${creds.credentialsAndRegion.profileLocation.value.absolutePath}
           |* Region: '${creds.credentialsAndRegion.region.getName}'
           |* IAM User:
           |  - UserName: '${creds.user.getUserName}'
           |  - Arn: '${creds.user.getArn}'
           |  - Created on: '${creds.user.getCreateDate}'
           |  - Last used on: '${creds.user.getPasswordLastUsed}'
           |* Credentials:
           |  - AWSAccessKeyId: '${creds.credentialsAndRegion.credentials.value.getAWSAccessKeyId}'
           |  - AWSSecretKey: '${creds.credentialsAndRegion.credentials.value.getAWSSecretKey}'
      """.stripMargin)
    },

    // code build tasks
    cbGenerateBuildSpec := CodeBuildOperations.generateBuildSpec(BuildSpecSettings(baseDirectory.value)),

    // lambda operations
    lambdaListFunctions := AwsLambdaOperations.listFunctions(clientAwsLambda.value),
    lambdaListFunctions := (lambdaListFunctions keepAs lambdaListFunctions).value,
    lambdaListFunctions := (lambdaListFunctions triggeredBy (compile in Compile)).value,

    lambdaGetFunction := {
      val functionName = Defaults.getForParser(lambdaListFunctions)((state, functions) => {
        val strings = functions.getOrElse(Nil).map(_.getFunctionName)
        Space ~> StringBasic.examples(strings: _*)
      }).parsed
      AwsLambdaOperations.getFunction(functionName, clientAwsLambda.value)
    },

    lambdaInvoke := {
      val (functionName, payload) = Defaults.getForParser(lambdaListFunctions)((state, functions) => {
        val strings = functions.getOrElse(Nil).map(_.getFunctionName)
        (Space ~> StringBasic.examples(strings: _*)) ~ (Space ~> StringBasic.examples("""{"foo":"bar"}"""))
      }).parsed
      AwsLambdaOperations.invoke(functionName, payload, clientAwsLambda.value)
    },

    lambdaMetrics := CloudWatchOperations.lambdaMetrics(clientCloudWatch.value),
    lambdaMetrics := (lambdaMetrics keepAs lambdaMetrics).value,
    lambdaMetrics := (lambdaMetrics triggeredBy lambdaListFunctions).value,

    lambdaLog := {
      val functionName = Defaults.getForParser(lambdaListFunctions)((_, functions) => {
        val strings = functions.getOrElse(Nil).map(_.getFunctionName)
        Space ~> StringBasic.examples(strings: _*)
      }).parsed
      val metrics = lambdaMetrics.value
    },

    cfDescribeStack := {
      val stackName = Def.spaceDelimited("stack-name").parsed
      CloudFormationOperations.describeStack(
        DescribeStackSettings.fromStackName(stackName.head),
        clientCloudFormation.value
      ).bimap(t => DescribeStackResponse(None, Option(t)), result => DescribeStackResponse(Option(result), None))
       .merge
    },

    cfDescribeStackEvents := {
      val stackName = Def.spaceDelimited("stack-name").parsed
      CloudFormationOperations.describeStackEvents(
        DescribeStackEventsSettings.fromStackName(stackName.head),
        clientCloudFormation.value
      ).bimap(t => DescribeStackEventsResponse(None, Option(t)), result => DescribeStackEventsResponse(Option(result), None))
        .merge
    },

    cfDeleteStack := {
      val stackName = Def.spaceDelimited("stack-name").parsed
      CloudFormationOperations.deleteStack(
        DeleteStackSettings.fromStackName(stackName.head),
        clientCloudFormation.value
      ).bimap(t => DeleteStackResponse(None, Option(t)), result => DeleteStackResponse(Option(result), None))
        .merge
    },

    createValidUsers := {
      val userList: List[CognitoUserDetails] = usersToCreate.value

      val users: List[Disjunction[String, ValidUser]] = userList.map { user ⇒
        AwsCognitoIdpOperations.adminCreateAndAuthUser(clientCognito.value, user.userName, user.password, user.userPoolId, user.clientId)
          .map(response ⇒ ValidUser(user.userName, response.getAuthenticationResult.getIdToken))
      }

      users.filter(_.isRight).flatMap(_.toList)
    },
  )
}