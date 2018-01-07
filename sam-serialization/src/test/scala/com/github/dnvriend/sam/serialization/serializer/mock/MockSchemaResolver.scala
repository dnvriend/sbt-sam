package com.github.dnvriend.sam.serialization.serializer.mock

import com.github.dnvriend.sam.serialization.resolver.SchemaResolver
import com.sksamuel.avro4s.SchemaFor
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema

object MockSchemaResolver {
  def apply[A: SchemaFor]: MockSchemaResolver[A] = {
    new MockSchemaResolver[A]
  }
}

class MockSchemaResolver[A](implicit schemaForA: SchemaFor[A]) extends LazyLogging with SchemaResolver {
  override def resolve(fingerprint: String): Option[Schema] = {
    logger.info("Resolving schema for fingerprint: '{}'", fingerprint)
    Option(schemaForA())
  }
}
