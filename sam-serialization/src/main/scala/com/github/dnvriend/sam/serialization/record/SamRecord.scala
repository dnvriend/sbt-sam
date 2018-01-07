package com.github.dnvriend.sam.serialization.record

import play.api.libs.json.{ Format, Json }

object SamRecord {
  implicit val format: Format[SamRecord] = Json.format
}

case class SamRecord(
    /**
     * The organizational unit of the schema
     */
    namespaceName: String,

    /**
     * The name of the schema
     */
    schemaName: String,
    /**
     * fingerprint that uniquely identifies the writer's schema
     */
    fingerprint: String,

    /**
     * Encoded payload
     */
    payload: String
)

