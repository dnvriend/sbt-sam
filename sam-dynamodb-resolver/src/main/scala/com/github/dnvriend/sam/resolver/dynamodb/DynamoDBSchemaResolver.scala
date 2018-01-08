package com.github.dnvriend.sam.resolver.dynamodb

import com.github.dnvriend.lambda.SamContext
import com.github.dnvriend.repo.dynamodb.DynamoDBJsonRepository
import com.github.dnvriend.sam.serialization.resolver.SchemaResolver
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema

import scala.util.Try
import scalaz.Scalaz._

class DynamoDBSchemaResolver(
                              ctx: SamContext,
                              tableName: String = "schema_by_fingerprint",
                              idAttributeName: String = "fingerprint",
                              payloadAttributeName: String = "json",
                              ) extends SchemaResolver with LazyLogging {
  logger.info(
    """
      |Creating DynamoDBSchemaResolver:
      |==========================
      |tableName: '{}'
      |idAttributeName: '{}'
      |payloadAttributeName: '{}'
    """.stripMargin,
    tableName,
    idAttributeName,
    payloadAttributeName,
  )
  private val repository = DynamoDBJsonRepository(tableName, ctx, idAttributeName, payloadAttributeName)

  override def resolve(fingerprint: String): Option[Schema] = {
    logger.info("Getting schema for fingerprint: '{}'", fingerprint)
    for {
      samSchema <- repository.find[SamSchema](fingerprint)
      compressed <- Try(AvroUtils.decodeBase64(samSchema.compressedAvroSchemaBase64)).toOption
      uncompressed <- Try(AvroUtils.decompress(compressed)).toOption
      avroJsonString <- Try(AvroUtils.decodeUtf8(uncompressed)).toOption
      schema <- Try(AvroUtils.parseAvro(avroJsonString)).toOption
    } yield schema
  }
}
