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

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.aws.task._
import sbt.complete.DefaultParsers._
import sbt._
import sbt.Keys._

object AwsPlugin extends AutoPlugin with AllOps {

  override def trigger: PluginTrigger = allRequirements

  val autoImport = AwsPluginKeys

  import autoImport._

  override def projectSettings = Seq(
    credentialsAndRegion := GetCredentialsProvider.getCredentialsAndRegion(
      awsAccessKeyId.?.value.map(_.wrap[AwsAccessKeyId]),
      awsSecretAccessKey.?.value.map(_.wrap[AwsSecretAccessKey]),
      awsProfile.?.value,
      awsRegion.?.value
    ),
    awsRegion := DEFAULT_REGION,
    awsProfile := DEFAULT_PROFILE,

    clientAwsLambda := AwsLambdaOperations.client(credentialsAndRegion.value),
    clientApiGateway := ApiGatewayOperations.client(credentialsAndRegion.value),
    clientDynamoDb := DynamoDbOperations.client(credentialsAndRegion.value),
    clientS3 := S3Operations.client(credentialsAndRegion.value),
    clientKinesis := KinesisOperations.client(credentialsAndRegion.value),
    clientSns := SNSOperations.client(credentialsAndRegion.value),
    clientCloudWatch := CloudWatchOperations.client(credentialsAndRegion.value),
    clientIam := IamOperations.client(credentialsAndRegion.value),
    clientCloudFormation := CloudFormationOperations.client(credentialsAndRegion.value),
    clientCodeBuild := CodeBuildOperations.client(credentialsAndRegion.value),
    clientXRay := XRayOperations.client(credentialsAndRegion.value),

    // iam operations
    iamUserInfo := IamOperations.getUser(clientIam.value),

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
  )
}