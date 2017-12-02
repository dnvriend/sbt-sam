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

package com.github.dnvriend.sbt.sam

import sbt._

object SAMPluginKeys {
  // serverless plugin base settings
  lazy val samStage = settingKey[String]("The stage to deploy the service to")
  lazy val samOptions = settingKey[Serverless.Options]("Returns the serverless settings to use when calling the serverless CLI")
  lazy val initServerlessPlugin = TaskKey[Unit]("initServerlessPlugin", "initializes the serverless plugin")

  // serverless plugin project information
  lazy val samFullClassLoader = TaskKey[ClassLoader]("full-classloader")
  lazy val samProjectConfiguration = taskKey[ProjectConfiguration]("Returns the Serverless Project Configuration")

  // serverless plugin tasks
  lazy val samVersion = taskKey[String]("Get the Serverless runtime version")
  lazy val samInfo = taskKey[Unit]("Show info about the Serverless project")
  lazy val samGetInfo = taskKey[Serverless.Info]("Gets info about the Serverless project")
  lazy val samDeploy = taskKey[Unit]("Deploy a service to AWS")
  lazy val samRemove = taskKey[Unit]("Remove a server from AWS")
  lazy val samDeployLambda = inputKey[Unit]("Update a single lambda function")
  lazy val samCreateServerlessYaml = taskKey[Unit]("Creates a 'serverless.yaml' file in the project base directory")
  lazy val samCreateCloudFormationCognitoTemplate = taskKey[Unit]("Creates a Cognito CloudFormation template in the serverless work dir")
  lazy val samCreateCloudFormationDynamoDbTemplate = taskKey[Unit]("Creates a DynamoDb CloudFormation template in the serverless work dir")
  lazy val samRunCloudFormationCognitoTemplate = taskKey[Unit]("Runs the Cognito CloudFormation template")
  lazy val samRunCloudFormationDynamoDbTemplate = taskKey[Unit]("Runs the DynamoDb CloudFormation template")
}
