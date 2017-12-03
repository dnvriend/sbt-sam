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

import sbt.Keys._
import sbt.{ Def, _ }
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin
import com.github.dnvriend.sbt.aws.AwsPlugin
import com.github.dnvriend.sbt.aws.AwsPluginKeys._
import com.github.dnvriend.sbt.aws.task.{ CloudFormationOperations, TemplateBody }
import com.github.dnvriend.sbt.sam.task._
import sbt.internal.inc.classpath.ClasspathUtilities

object SAMPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin && AssemblyPlugin && AwsPlugin

  val autoImport = SAMPluginKeys
  import autoImport._

  override def projectSettings = Seq(
    samStage := SAM_DEFAULT_STAGE,
    samProjectClassLoader := {
      val scalaInstance = Keys.scalaInstance.value
      val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
      val classDirectory: File = (Keys.classDirectory in Compile).value
      val targetDir: File = Keys.target.value
      val classpath = Seq(classDirectory, targetDir) ++ fullClasspath
      val cl: ClassLoader = ClasspathUtilities.makeLoader(classpath, scalaInstance)
      cl
    },

    discoveredClassFiles := ((compile in Compile) map DiscoverClasses.run keepAs discoveredClassFiles triggeredBy (compile in Compile)).value,

    discoveredClasses := {
      val baseDir: File = (classDirectory in Compile).value
      val projectClassFiles: Set[File] = discoveredClassFiles.value
      val classLoader: ClassLoader = samProjectClassLoader.value
      DiscoverProjectClasses.run(projectClassFiles, baseDir, classLoader)
    },
    discoveredClasses := (discoveredClasses triggeredBy discoveredClassFiles).value,
    discoveredClasses := (discoveredClasses keepAs discoveredClasses).value,

    discoveredLambdas := DiscoverLambdas.run(discoveredClasses.value),
    discoveredLambdas := (discoveredLambdas triggeredBy discoveredClasses).value,
    discoveredLambdas := (discoveredLambdas keepAs discoveredLambdas).value,

    classifiedLambdas := ClassifyLambdas.run(discoveredLambdas.value, samStage.value),
    classifiedLambdas := (classifiedLambdas triggeredBy discoveredLambdas).value,
    classifiedLambdas := (classifiedLambdas keepAs classifiedLambdas).value,

    // generate the sam cloud formation template
    samGenerateTemplate := {
      CreateSamTemplate.run(
        classifiedLambdas.value,
        iamUserInfo.value,
        credentialsAndRegion.value,
        samStage.value,
        Keys.description.?.value.getOrElse(Keys.name.value))
    },

    // validate the sam cloud formation template
    samValidate := {
      val template = samGenerateTemplate.value
      val client = clientCloudFormation.value
      CloudFormationOperations.validateTemplate(template, client)
    }
  )
}