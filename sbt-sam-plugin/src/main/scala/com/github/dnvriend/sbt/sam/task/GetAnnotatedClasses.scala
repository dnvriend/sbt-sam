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

package com.github.dnvriend.sbt.sam.task

import scala.tools.nsc.classpath._
import sbt._

object GetAnnotatedClasses {
  private final val LOGGER_NAME = "GetAnnotatedClasses"
  def run(
    baseDir: File,
    allClassFilesInClassDir: Set[File],
    cl: ClassLoader)(implicit logger: Logger): Set[String] = {

    val relativizePath = createRelativizer(baseDir)
    val allClasses: Set[(String, String)] = allClassFilesInClassDir
      .flatMap(classFile => relativizePath(classFile).toSeq)
      .map(FileUtils.stripClassExtension)
      .map(fileSyntaxToPackageSyntax)
      .map(PackageNameUtils.separatePkgAndClassNames)

    val onlyClassesAsClass: Set[(String, Class[_])] = allClasses.map {
      case (packageName, className) => (s"$packageName.$className", cl.loadClass(s"$packageName.$className"))
    }

    val fqcnThatAreSchemas: Set[String] = onlyClassesAsClass
      .filter(filterDapSchemas)
      .map(extractPackageName)

    fqcnThatAreSchemas
  }

  def filterDapSchemas(t: (String, Class[_])): Boolean = t match {
    case (packageName, classToExamine) => classToExamine.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains("ANNOTATION_NAME"))
  }

  def extractPackageName(t: (String, Class[_])): String = t match {
    case (packageName, classToExamine) => packageName
  }

  def fileSyntaxToPackageSyntax(fileSyntax: String): String = {
    fileSyntax.replace("/", ".")
  }

  def createRelativizer(baseDir: File): sbt.File => Option[String] = {
    IO.relativize(baseDir, _: File)
  }
}