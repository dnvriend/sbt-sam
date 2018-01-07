package com.github.dnvriend.sam.schema.plugin.service

import sbt._

object FileSystemServices {
  final val PluginWorkDir = "sam-schema-plugin"
  def createWorkDirInTargetDir(targetDir: File): File = {
    val pluginWorkDir: File = targetDir / PluginWorkDir
    //    println("Creating plugin work dir: " + pluginWorkDir)
    IO.createDirectory(pluginWorkDir)
    pluginWorkDir
  }
}
