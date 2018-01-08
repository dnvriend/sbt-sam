package com.github.dnvriend.sam.resolver.dynamodb

import play.api.libs.json.{Format, Json}

object SamSchema {
  implicit val format: Format[SamSchema] = Json.format[SamSchema]
}

final case class SamSchema(
                            /**
                              * A vector identifies a schema. Its format is namespaceName:schemaName:version
                              */
                            vector: String,

                            /**
                              * The namespace of the schema. A namespace is a unit of administration and
                              * segments logical domains
                              */
                            namespaceName: String,

                            /**
                              * The name of the schema. Every schema has a name and must be unique within a
                              * namespace
                              */
                            schemaName: String,

                            /**
                              * The version of a schema. Schemas actually do not have a version, but a version number
                              * that has monotonic properties, appended to a schema, makes it possible, for the developer,
                              * to reason about schema evolution.
                              */
                            version: Long,

                            /**
                              * Fingerprint is a SHA-256 digest of the normalized schema. The SHA-256 is encoded as base64.
                              * The schema evolution strategy of SAM only uses fingerprints to reason about shapes and
                              * the relationship between shapes.
                              */
                            fingerprintBase64: String,

                            /**
                              * Fingerprint is a SHA-256 digest of the normalized schema. The SHA-256 is encoded as hex string.
                              * The schema evolution strategy of SAM only uses fingerprints to reason about shapes and
                              * the relationship between shapes.
                              */
                            fingerprintHexString: String,

                            /**
                              * The actual avro schema compressed and encoded as base64
                              */
                            compressedAvroSchemaBase64: String,

                            /**
                              * The actual avro schema compressed and encoded as hex string
                              */
                            compressedAvroSchemaHexString: String,
                          )
