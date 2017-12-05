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

import com.amazonaws.services.cloudformation.model.ValidateTemplateResult
import com.github.dnvriend.sbt.aws.task.TemplateBody
import com.github.dnvriend.sbt.sam.state.ProjectState
import com.github.dnvriend.sbt.sam.task.{ LambdaHandler, ProjectClass, ProjectConfiguration, ProjectLambda }
import sbt._

object SAMPluginKeys {
  // sam attributes
  lazy val samAttributeProjectState = SettingKey[ProjectState]("The state of the sam project")

  // sam settings
  lazy val samStage = settingKey[String]("The stage to deploy the service to")
  lazy val samS3BucketName = settingKey[String]("The S3 deployment bucket name for the sam project")
  lazy val samCFTemplateName = settingKey[String]("The cloudformation template name for the sam project")
  lazy val samResourcePrefixName = settingKey[String]("The prefix name to use when creating AWS resources like Lambdas, DynamoDB tables, Kinesis topics and so on")

  // sam worker tasks
  lazy val samProjectClassLoader = TaskKey[ClassLoader]("sam's project classloader")
  lazy val discoveredClassFiles = taskKey[Set[File]]("")
  lazy val discoveredClasses = taskKey[Set[ProjectClass]]("")
  lazy val discoveredLambdas = taskKey[Set[ProjectLambda]]("")
  lazy val classifiedLambdas = taskKey[Set[LambdaHandler]]("")
  lazy val discoveredResources = taskKey[Set[Class[_]]]("")
  lazy val samProjectConfiguration = taskKey[ProjectConfiguration]("The sam project configuration")

  // sam tasks
  lazy val samInfo = taskKey[Unit]("Show info the service")
  lazy val samDeploy = taskKey[Unit]("Deploy a service")
  lazy val samRemove = taskKey[Unit]("Remove a service")
  lazy val samUpdate = inputKey[Unit]("Update a service")
  lazy val samGenerateTemplate = taskKey[TemplateBody]("Generate the cloudformation template")
  lazy val samValidate = taskKey[Unit]("Validates the cloud formation template")
}
