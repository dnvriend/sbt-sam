package com.github.dnvriend.sbt.sam.task

import sbt._

import scala.tools.nsc.classpath.{ FileUtils, PackageNameUtils }

final case class ProjectClass(
    packageName: PackageName,
    className: ClassName,
    cl: Class[_])
final case class PackageName(value: String)
final case class ClassName(value: String)

object DiscoverProjectClasses {
  def run(
    projectClassFiles: Set[File],
    baseDir: File,
    cl: ClassLoader): Set[ProjectClass] = {

    val relativizePath = createRelativizer(baseDir)
    val allClasses: Set[(String, String)] = projectClassFiles
      .flatMap(classFile => relativizePath(classFile).toSeq)
      .map(FileUtils.stripClassExtension)
      .map(fileSyntaxToPackageSyntax)
      .map(PackageNameUtils.separatePkgAndClassNames)

    allClasses.map {
      case (packageName, className) =>
        ProjectClass(
          PackageName(packageName),
          ClassName(className),
          cl.loadClass(s"$packageName.$className")
        )
    }
  }

  def fileSyntaxToPackageSyntax(fileSyntax: String): String = {
    fileSyntax.replace("/", ".")
  }
  def createRelativizer(baseDir: File): sbt.File => Option[String] = {
    IO.relativize(baseDir, _: File)
  }
}
