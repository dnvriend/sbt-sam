package com.github.dnvriend.sam.schema.plugin
package service

import sbt._

import scala.tools.nsc.classpath.PackageNameUtils
import scalaj.http._
import scalaz._
import scalaz.Scalaz._

object SchemaService {
  def push(
    url: String,
    authToken: String,
    fqcn: String,
    schema: String
  ): Unit = {
    val (namespaceName, schemaName) = PackageNameUtils.separatePkgAndClassNames(fqcn)
    val response = SamSchemaClient.put(s"$url/namespaces/$namespaceName/schemas/$schemaName", authToken, schema)
    val msg = response.bimap(_.getMessage, _.body).merge
    println(msg)
  }

  def pull(
    schemaDependencies: Seq[ModuleID],
    schemaDirectory: File,
    url: String,
    authToken: String
  ): Seq[File] = {
    val result: DTry[List[org.apache.avro.Schema]] = schemaDependencies.toList.map { vector =>
      val namespaceName: String = vector.organization
      val schemaName: String = vector.name
      val version: String = vector.revision
      SamSchemaClient.get(s"$url/namespaces/$namespaceName/schemas/$schemaName/versions/$version", authToken)
        .map(_.body).map(parseAvro)
    }.sequenceU

    result.fold(logError, xs => xs.map(schema => write(schemaDirectory, schema)))
  }

  def logError(t: Throwable): Seq[File] = {
    println(t.getMessage)
    Seq.empty[File]
  }

  def parseAvro(schema: String): org.apache.avro.Schema = {
    new org.apache.avro.Schema.Parser().parse(schema)
  }

  def convertVectorToFile(baseDir: File, namespaceName: String, schemaName: String): File = {
    namespaceName.split("\\.").toList.foldLeft(baseDir)((c, e) => c / e) / s"$schemaName.avsc"
  }

  def write(baseDir: File, schema: org.apache.avro.Schema): File = {
    val fullFile: File = convertVectorToFile(baseDir, schema.getNamespace, schema.getName)
    IO.write(fullFile, schema.toString(false))
    fullFile
  }
}

object SamSchemaClient {
  def put(url: String, authToken: String, schema: String): DTry[HttpResponse[String]] = {
    Http(url)
      .headers("Authorization" -> authToken)
      .timeout(Int.MaxValue, Int.MaxValue)
      .put(schema)
      .asString
  }.safe

  def get(url: String, authToken: String): DTry[HttpResponse[String]] = {
    Http(url)
      .headers("Authorization" -> authToken)
      .timeout(Int.MaxValue, Int.MaxValue)
      .asString
  }.safe

}
