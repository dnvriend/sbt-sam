package com.github.dnvriend.sam.schema.plugin

import sbt._
import sbt.Keys._

object SamSchemaPluginKeys {
  // settings
  lazy val schemaDir = settingKey[File]("The directory to store pulled schemas")
  lazy val schemaUserPoolId = settingKey[String]("The schema user pool id to authenticate against")
  lazy val schemaClientId = settingKey[String]("The schema client id from aws cognito")
  lazy val schemaUsername = settingKey[String]("The schema username")
  lazy val schemaPassword = settingKey[String]("The schema password")
  lazy val schemaRepositoryUrl = settingKey[String]("The location of sam-schema-repo")
  lazy val schemaDependencies = settingKey[Seq[ModuleID]]("A list of schemas to depend upon")

  // worker tasks
  lazy val discoveredClasses = taskKey[Set[File]]("The set of compiled classes")
  lazy val discoveredSchemasFQCN = taskKey[Set[String]]("The set of fully qualified class names of the detected schemas")
  lazy val discoveredSchemas = taskKey[List[SamSchema]]("The set of discovered sam schemas")
  lazy val schemaClassLoader = taskKey[ClassLoader]("Classloader to use when loading schema classes")
  lazy val createPluginWorkDir = taskKey[File]("Creates the work dir for the plugin")
  lazy val authenticationToken = taskKey[String]("Generates an authentication token")

  // script tasks
  lazy val createScript = taskKey[Unit]("This task compiles the case class to avro schema files")
  lazy val compileScript = taskKey[Unit]("This task compiles the Script.scala file")
  lazy val createAndCompileScript = taskKey[Unit]("Create and compile the Script.scala file")

  lazy val schemaList = taskKey[Unit]("List detected sam schemas")
  lazy val schemaPush = inputKey[Unit]("Pushes a selected schema to the schema repository")
  lazy val schemaPull = taskKey[Seq[File]]("Pulls schemas defined in 'schemaDependencies'")
  lazy val schemaCompile = taskKey[Seq[File]]("Compiles schemas defined in 'schemaDependencies' to case classes")
}
