package com.github.dnvriend.sam.serialization
package resolver

import java.util.concurrent.TimeUnit

import org.apache.avro.Schema

import scalaj.http._
import scalaz.Scalaz._
import com.google.common.cache.{ CacheBuilder, CacheLoader, LoadingCache }
import com.typesafe.scalalogging.LazyLogging

class HttpResolver(
    url: String,
    authToken: String,
    maximumCacheSize: Int = 50,
    expireAfterWriteSeconds: Int = 60
) extends SchemaResolver with LazyLogging {
  logger.info(
    """
      |Creating HttpResolver:
      |==========================
      |url: '{}'
      |authToken: '{}'
      |maximumCacheSize: '{}'
      |expireAfterWriteSeconds: '{}'
    """.stripMargin,
    url,
    authToken,
    maximumCacheSize,
    expireAfterWriteSeconds
  )
  private val schemaCache: LoadingCache[String, Schema] = CacheBuilder.newBuilder()
    .maximumSize(maximumCacheSize)
    .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
    .build(new CacheLoader[String, Schema] {
      override def load(key: String): Schema = {
        downloadSchema(key)
      }
    })

  override def resolve(fingerprint: String): Option[Schema] = {
    logger.info("Resolving schema for fingerprint: '{}'", fingerprint)
    val result = schemaCache.get(fingerprint).safe
    if (result.isLeft) {
      val msg = result.swap.foldMap(_.getMessage)
      logger.error("Error downloading schema for fingerprint: '{}', '{}'", fingerprint, msg)
    }
    result.toOption
  }

  def downloadSchema(fingerprint: String): Schema = {
    logger.info("Downloading schema for fingerprint: '{}'", fingerprint)
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

