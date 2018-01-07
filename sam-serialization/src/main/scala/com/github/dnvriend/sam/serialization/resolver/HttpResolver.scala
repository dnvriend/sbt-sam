package com.github.dnvriend.sam.serialization
package resolver

import org.apache.avro.Schema

import scalaj.http._
import scalaz.Scalaz._
import com.google.common.cache.{ CacheBuilder, CacheLoader, LoadingCache }

class HttpResolver(url: String, authToken: String) extends SchemaResolver {
  private val schemaCache: LoadingCache[String, Schema] = CacheBuilder.newBuilder()
    .maximumSize(50)
    .build(new CacheLoader[String, Schema] {
      override def load(key: String): Schema = {
        downloadSchema(key)
      }
    })

  override def resolve(fingerprint: String): Option[Schema] = {
    val result = schemaCache.get(fingerprint).safe
    if (result.isLeft) {
      val msg = result.swap.foldMap(_.getMessage)
      println(s"Error downloading schema for fingerprint: $fingerprint, $msg")
    }
    result.toOption
  }

  def downloadSchema(fingerprint: String): Schema = {
    println(s"Downloading schema for fingerprint: '$fingerprint'")
    parseAvro(Http(s"$url/fingerprint/$fingerprint")
      .headers("Authorization" -> authToken)
      .timeout(Int.MaxValue, Int.MaxValue)
      .asString
      .body)
  }

  def parseAvro(schema: String): org.apache.avro.Schema = {
    new org.apache.avro.Schema.Parser().parse(schema)
  }
}

