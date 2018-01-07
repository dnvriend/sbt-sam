package com.github.dnvriend.sam.schema.plugin

import avro4s.{Avro4sSbtPlugin, Import}
import com.github.dnvriend.sam.schema.plugin.service._
import com.github.dnvriend.sbt.aws.AwsPlugin
import com.github.dnvriend.sbt.aws.AwsPluginKeys._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._

/**
  * SamSchemaPlugin must be explicitly enabled on projects
  */
object SamSchemaPlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin && Avro4sSbtPlugin && AwsPlugin

  val autoImport = SamSchemaPluginKeys

  import autoImport._

  lazy val defaultSettings: Seq[Setting[_]] = Seq(
    schemaDir := (resourceManaged in Import.avroIdl2Avro).value,
    schemaDependencies := Seq.empty[ModuleID],
    createPluginWorkDir := FileSystemServices.createWorkDirInTargetDir(target.value),
    createPluginWorkDir := (createPluginWorkDir triggeredBy (compile in Compile)).value,

    discoveredClasses := ((compile in Compile) map DiscoverClassesService.run keepAs discoveredClasses triggeredBy (compile in Compile)).value,
    discoveredSchemasFQCN := {
      val baseDir: File = (classDirectory in Compile).value
      val classes: Set[File] = discoveredClasses.value
      val cl: ClassLoader = schemaClassLoader.value
      val schemas: Set[String] = GetAnnotatedClassesService.run(baseDir, classes, cl)
      schemas
    },
    discoveredSchemasFQCN := (discoveredSchemasFQCN keepAs discoveredSchemasFQCN).value,

    schemaClassLoader := {
      val scalaInstance = Keys.scalaInstance.value
      val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
      val classDirectory: File = (Keys.classDirectory in Compile).value
      val targetDir: File = Keys.target.value
      val sbtDapWorkDir: File = targetDir / FileSystemServices.PluginWorkDir
      val classpath = Seq(sbtDapWorkDir, classDirectory, targetDir) ++ fullClasspath
      val cl: ClassLoader = sbt.internal.inc.classpath.ClasspathUtilities.makeLoader(classpath, scalaInstance)
      cl
    },

    createAndCompileScript := Def.sequential(createPluginWorkDir, createScript, compileScript).value,
    createAndCompileScript := (createAndCompileScript triggeredBy discoveredClasses).value,

    createScript := ScriptService.createScriptFile(createPluginWorkDir.value, discoveredSchemasFQCN.value.toList),
    compileScript := {
      val compilers = Keys.compilers.value
      val fileToCompile: File = createPluginWorkDir.value / "Script.scala"
      val classPath: Seq[File] = (fullClasspath in Compile).value.map(_.data)
      val outputDir: File = (classDirectory in Compile).value
      val options: Seq[String] = (scalacOptions in Compile).value
      val inputs = (compileInputs in Compile in compile).value
      val cache = inputs.setup().cache()
      val log = streams.value.log
      ScriptService.compileScript(compilers, fileToCompile, classPath, outputDir, options, cache, log)
    },

    discoveredSchemas := DiscoverSchemaService.discoverSchemas(schemaClassLoader.value, discoveredSchemasFQCN.value),
    discoveredSchemas := (discoveredSchemas keepAs discoveredSchemas triggeredBy createAndCompileScript).value,

    // schema tasks
    schemaList := {
      val log = streams.value.log
      val schemas: List[SamSchema] = discoveredSchemas.value
      schemas.foreach {
        case SamSchema(fqcn, schema) =>
          log.info(s"* schema: '$fqcn'\n$schema\n")
      }
    },

    authenticationToken := {
      val client = clientCognito.value
      val userPoolId: String = SettingsService.userPoolId(schemaUserPoolId.?.value)
      val clientId: String = SettingsService.clientId(schemaClientId.?.value)
      val username: String = SettingsService.username(schemaUsername.?.value)
      val password: String = SettingsService.password(schemaPassword.?.value)
      AuthenticationService.getIdToken(userPoolId, clientId, username, password, client)
    },

    // push a schema to the repository
    schemaPush := {
      val url: String = SettingsService.schemaUrl(schemaRepositoryUrl.?.value)
      val schemas: List[SamSchema] = discoveredSchemas.value
      val authToken = authenticationToken.value
      val selectedSchema = Defaults.getForParser(discoveredSchemas)((state, schemas) => {
        val strings = schemas.getOrElse(Nil).map(_.fqcn)
        Space ~> StringBasic.examples(strings: _*)
      }).parsed
      schemas.find(_.fqcn == selectedSchema).foreach {
        case SamSchema(fqcn, schema) =>
          SchemaService.push(url, authToken, fqcn, schema)
      }
    },
    schemaPush := (schemaPush dependsOn createAndCompileScript).evaluated,

    schemaPull := {
      val url: String = SettingsService.schemaUrl(schemaRepositoryUrl.?.value)
      val authToken: String = authenticationToken.value
      val dependencies: Seq[ModuleID] = schemaDependencies.value.toList
      val schemaDirectory: File = schemaDir.value
      SchemaService.pull(dependencies, schemaDirectory, url, authToken)
    },

    schemaCompile := Import.avro2Class.value,
  )

  override def projectSettings: Seq[Setting[_]] = {
    defaultSettings
  }
}
