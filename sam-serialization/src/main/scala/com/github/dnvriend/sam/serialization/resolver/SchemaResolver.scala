package com.github.dnvriend.sam.serialization.resolver

import org.apache.avro.Schema

trait SchemaResolver {
  def resolve(fingerprint: String): Option[Schema]
}
