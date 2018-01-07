package com.github.dnvriend.sam.schema.plugin.service

import sbt._

import scala.tools.nsc.classpath.{ FileUtils, PackageNameUtils }

object GetAnnotatedClassesService {
  private final val ANNOTATION_NAME = "SamSchema"
  def run(
    baseDir: File,
    allClassFilesInClassDir: Set[File],
    cl: ClassLoader): Set[String] = {

    val relativizePath = createRelativizer(baseDir)
    val allClasses: Set[(String, String)] = allClassFilesInClassDir
      .flatMap(classFile => relativizePath(classFile).toSeq)
      .map(FileUtils.stripClassExtension)
      .map(fileSyntaxToPackageSyntax)
      .map(PackageNameUtils.separatePkgAndClassNames)

    val onlyClassesAsClass: Set[(String, Class[_])] = allClasses.map {
      case (packageName, className) => (s"$packageName.$className", cl.loadClass(s"$packageName.$className"))
    }

    onlyClassesAsClass
      .filter(filterDapSchemas)
      .map(extractPackageName)
  }

  def filterDapSchemas(t: (String, Class[_])): Boolean = t match {
    case (packageName, classToExamine) => classToExamine.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains(ANNOTATION_NAME))
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
